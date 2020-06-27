/*
Mangadex@Home
Copyright (c) 2020, MangaDex Network
This file is part of MangaDex@Home.

MangaDex@Home is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

MangaDex@Home is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this MangaDex@Home.  If not, see <http://www.gnu.org/licenses/>.
 */
/* ktlint-disable no-wildcard-imports */
package mdnet.base

import ch.qos.logback.classic.LoggerContext
import com.fasterxml.jackson.module.kotlin.readValue
import mdnet.base.Constants.JACKSON
import mdnet.base.Main.dieWithError
import mdnet.base.server.getServer
import mdnet.base.server.getUiServer
import mdnet.base.settings.ClientSettings
import mdnet.base.settings.ServerSettings
import mdnet.cache.DiskLruCache
import mdnet.cache.HeaderMismatchException
import org.http4k.server.Http4kServer
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.time.Instant
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

sealed class State
// server is not running
object Uninitialized : State()
// server has shut down
object Shutdown : State()
// server is in the process of shutting down
data class GracefulShutdown(val lastRunning: Running, val counts: Int = 0, val nextState: State = Uninitialized, val action: () -> Unit = {}) : State()
// server is currently running
data class Running(val server: Http4kServer, val settings: ServerSettings) : State()

class MangaDexClient(private val clientSettings: ClientSettings) {
    // this must remain singlethreaded because of how the state mechanism works
    private val executorService = Executors.newSingleThreadScheduledExecutor()
    // state must only be accessed from the thread on the executorService
    private var state: State = Uninitialized

    private val serverHandler: ServerHandler = ServerHandler(clientSettings)
    private val statsMap: MutableMap<Instant, Statistics> = Collections
        .synchronizedMap(object : LinkedHashMap<Instant, Statistics>(240) {
            override fun removeEldestEntry(eldest: Map.Entry<Instant, Statistics>): Boolean {
                return this.size > 240
            }
        })
    private val statistics: AtomicReference<Statistics> = AtomicReference(Statistics())
    private val isHandled: AtomicBoolean = AtomicBoolean(false)
    private var webUi: Http4kServer? = null
    private val cache: DiskLruCache

    init {
        try {
            cache = DiskLruCache.open(
                File("cache"), 1, 1,
                clientSettings.maxCacheSizeInMebibytes * 1024 * 1024 /* MiB to bytes */
            )
            cache.get("statistics")?.use {
                statistics.set(JACKSON.readValue<Statistics>(it.getInputStream(0)))
            }
        } catch (e: HeaderMismatchException) {
            LOGGER.warn("Cache version may be outdated - remove if necessary")
            dieWithError(e)
        } catch (e: IOException) {
            LOGGER.warn("Cache may be corrupt - remove if necessary")
            dieWithError(e)
        }
    }

    fun runLoop() {
        loginAndStartServer()
        statsMap[Instant.now()] = statistics.get()

        if (clientSettings.webSettings != null) {
            webUi = getUiServer(clientSettings.webSettings, statistics, statsMap)
            webUi!!.start()
        }
        if (LOGGER.isInfoEnabled) {
            LOGGER.info("Mangadex@Home Client initialized. Starting normal operation.")
        }

        executorService.scheduleAtFixedRate({
            try {
                statistics.updateAndGet {
                    it.copy(bytesOnDisk = cache.size())
                }
                statsMap[Instant.now()] = statistics.get()
                val editor = cache.edit("statistics")
                if (editor != null) {
                    JACKSON.writeValue(editor.newOutputStream(0), statistics.get())
                    editor.commit()
                }
            } catch (e: Exception) {
                LOGGER.warn("Statistics update failed", e)
            }
        }, 15, 15, TimeUnit.SECONDS)

        var lastBytesSent = statistics.get().bytesSent
        executorService.scheduleAtFixedRate({
            try {
                lastBytesSent = statistics.get().bytesSent

                val state = this.state
                if (state is GracefulShutdown) {
                    if (LOGGER.isInfoEnabled) {
                        LOGGER.info("Aborting graceful shutdown started due to hourly bandwidth limit")
                    }
                    this.state = state.lastRunning
                }
                if (state is Uninitialized) {
                    if (LOGGER.isInfoEnabled) {
                        LOGGER.info("Restarting server stopped due to hourly bandwidth limit")
                    }
                    loginAndStartServer()
                }
            } catch (e: Exception) {
                LOGGER.warn("Hourly bandwidth check failed", e)
            }
        }, 1, 1, TimeUnit.HOURS)

        val timesToWait = clientSettings.gracefulShutdownWaitSeconds / 15
        executorService.scheduleAtFixedRate({
            try {
                val state = this.state
                if (state is GracefulShutdown) {
                    when {
                        state.counts == 0 -> {
                            if (LOGGER.isInfoEnabled) {
                                LOGGER.info("Starting graceful shutdown")
                            }
                            logout()
                            isHandled.set(false)
                            this.state = state.copy(counts = state.counts + 1)
                        }
                        state.counts == timesToWait || !isHandled.get() -> {
                            if (LOGGER.isInfoEnabled) {
                                if (!isHandled.get()) {
                                    LOGGER.info("No requests received, shutting down")
                                } else {
                                    LOGGER.info("Max tries attempted (${state.counts} out of $timesToWait), shutting down")
                                }
                            }

                            stopServer(state.nextState)
                            state.action()
                        }
                        else -> {
                            if (LOGGER.isInfoEnabled) {
                                LOGGER.info(
                                    "Waiting another 15 seconds for graceful shutdown (${state.counts} out of $timesToWait)"
                                )
                            }
                            isHandled.set(false)
                            this.state = state.copy(counts = state.counts + 1)
                        }
                    }
                }
            } catch (e: Exception) {
                LOGGER.warn("Main loop failed", e)
            }
        }, 15, 15, TimeUnit.SECONDS)

        executorService.scheduleWithFixedDelay({
            try {
                val state = this.state
                if (state is Running) {
                    val currentBytesSent = statistics.get().bytesSent - lastBytesSent
                    if (clientSettings.maxMebibytesPerHour != 0L && clientSettings.maxMebibytesPerHour * 1024 * 1024 /* MiB to bytes */ < currentBytesSent) {
                        if (LOGGER.isInfoEnabled) {
                            LOGGER.info("Shutting down server as hourly bandwidth limit reached")
                        }
                        this.state = GracefulShutdown(lastRunning = state)
                    } else {
                        pingControl()
                    }
                }
            } catch (e: Exception) {
                LOGGER.warn("Graceful shutdown checker failed", e)
            }
        }, 45, 45, TimeUnit.SECONDS)
    }

    private fun pingControl() {
        val state = this.state as Running

        val newSettings = serverHandler.pingControl(state.settings)
        if (newSettings != null) {
            if (LOGGER.isInfoEnabled) {
                LOGGER.info("Server settings received: $newSettings")
            }

            if (newSettings.latestBuild > Constants.CLIENT_BUILD) {
                if (LOGGER.isWarnEnabled) {
                    LOGGER.warn(
                        "Outdated build detected! Latest: ${newSettings.latestBuild}, Current: ${Constants.CLIENT_BUILD}"
                    )
                }
            }
            if (newSettings.tls != null || newSettings.imageServer != state.settings.imageServer) {
                // certificates or upstream url must have changed, restart webserver
                if (LOGGER.isInfoEnabled) {
                    LOGGER.info("Doing internal restart of HTTP server to refresh certs/upstream URL")
                }
                this.state = GracefulShutdown(lastRunning = state) {
                    loginAndStartServer()
                }
            }
        } else {
            if (LOGGER.isInfoEnabled) {
                LOGGER.info("Server ping failed - ignoring")
            }
        }
    }

    private fun loginAndStartServer() {
        this.state as Uninitialized

        val serverSettings = serverHandler.loginToControl()
            ?: dieWithError("Failed to get a login response from server - check API secret for validity")
        val server = getServer(cache, serverSettings, clientSettings, statistics, isHandled).start()

        if (serverSettings.latestBuild > Constants.CLIENT_BUILD) {
            if (LOGGER.isWarnEnabled) {
                LOGGER.warn(
                    "Outdated build detected! Latest: ${serverSettings.latestBuild}, Current: ${Constants.CLIENT_BUILD}"
                )
            }
        }

        state = Running(server, serverSettings)
        if (LOGGER.isInfoEnabled) {
            LOGGER.info("Internal HTTP server was successfully started")
        }
    }

    private fun logout() {
        serverHandler.logoutFromControl()
    }

    private fun stopServer(nextState: State = Uninitialized) {
        val state = this.state.let {
            when (it) {
                is Running ->
                    it
                is GracefulShutdown ->
                    it.lastRunning
                else ->
                    throw AssertionError()
            }
        }

        if (LOGGER.isInfoEnabled) {
            LOGGER.info("Shutting down HTTP server")
        }
        state.server.stop()
        if (LOGGER.isInfoEnabled) {
            LOGGER.info("Internal HTTP server has shut down")
        }
        this.state = nextState
    }

    fun shutdown() {
        LOGGER.info("Mangadex@Home Client stopping")

        val latch = CountDownLatch(1)
        executorService.schedule({
            val state = this.state
            if (state is Running) {
                this.state = GracefulShutdown(state, nextState = Shutdown) {
                    latch.countDown()
                }
            } else if (state is GracefulShutdown) {
                this.state = state.copy(nextState = Shutdown) {
                    latch.countDown()
                }
            } else if (state is Uninitialized || state is Shutdown) {
                this.state = Shutdown
                latch.countDown()
            }
        }, 0, TimeUnit.SECONDS)
        latch.await()

        webUi?.close()
        try {
            cache.close()
        } catch (e: IOException) {
            LOGGER.error("Cache failed to close", e)
        }

        executorService.shutdown()
        LOGGER.info("Mangadex@Home Client stopped")

        (LoggerFactory.getILoggerFactory() as LoggerContext).stop()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MangaDexClient::class.java)
    }
}

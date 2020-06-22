package mdnet.base

import ch.qos.logback.classic.LoggerContext
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.module.kotlin.readValue
import mdnet.base.Constants.JACKSON
import mdnet.base.settings.ClientSettings
import org.slf4j.LoggerFactory
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.regex.Pattern
import kotlin.system.exitProcess

object Main {
    private val LOGGER = LoggerFactory.getLogger(Main::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        println(
            "Mangadex@Home Client ${Constants.CLIENT_VERSION} (Build ${Constants.CLIENT_BUILD}) initializing"
        )
        println("Copyright (c) 2020, MangaDex Network")

        var file = "settings.json"
        if (args.size == 1) {
            file = args[0]
        } else if (args.isNotEmpty()) {
            dieWithError("Expected one argument: path to config file, or nothing")
        }

        val settings = try {
            JACKSON.readValue<ClientSettings>(FileReader(file))
        } catch (e: UnrecognizedPropertyException) {
            dieWithError("'${e.propertyName}' is not a valid setting")
        } catch (e: JsonProcessingException) {
            dieWithError(e)
        } catch (ignored: IOException) {
            ClientSettings().also {
                LOGGER.warn("Settings file {} not found, generating file", file)
                try {
                    FileWriter(file).use { writer -> JACKSON.writeValue(writer, it) }
                } catch (e: IOException) {
                    dieWithError(e)
                }
            }
        }.apply(::validateSettings)

        if (LOGGER.isInfoEnabled) {
            LOGGER.info("Client settings loaded: {}", settings)
        }
        val client = MangaDexClient(settings)
        Runtime.getRuntime().addShutdownHook(Thread { client.shutdown() })
        client.runLoop()
    }

    fun dieWithError(e: Throwable): Nothing {
        if (LOGGER.isErrorEnabled) {
            LOGGER.error("Critical Error", e)
        }
        (LoggerFactory.getILoggerFactory() as LoggerContext).stop()
        exitProcess(1)
    }

    fun dieWithError(error: String): Nothing {
        if (LOGGER.isErrorEnabled) {
            LOGGER.error("Critical Error: {}", error)
        }
        (LoggerFactory.getILoggerFactory() as LoggerContext).stop()
        exitProcess(1)
    }

    private fun validateSettings(settings: ClientSettings) {
        if (!isSecretValid(settings.clientSecret)) dieWithError("Config Error: API Secret is invalid, must be 52 alphanumeric characters")
        if (settings.clientPort == 0) {
            dieWithError("Config Error: Invalid port number")
        }
        if (settings.maxCacheSizeInMebibytes < 1024) {
            dieWithError("Config Error: Invalid max cache size, must be >= 1024 MiB (1GiB)")
        }
        if (settings.threads < 4) {
            dieWithError("Config Error: Invalid number of threads, must be >= 4")
        }
        if (settings.maxMebibytesPerHour < 0) {
            dieWithError("Config Error: Max bandwidth must be >= 0")
        }
        if (settings.maxKilobitsPerSecond < 0) {
            dieWithError("Config Error: Max burst rate must be >= 0")
        }
        if (settings.gracefulShutdownWaitSeconds < 15) {
            dieWithError("Config Error: Graceful shutdown wait be >= 15")
        }
        if (settings.webSettings != null) {
            if (settings.webSettings.uiPort == 0) {
                dieWithError("Config Error: Invalid UI port number")
            }
        }
    }

    private const val CLIENT_KEY_LENGTH = 52
    private fun isSecretValid(clientSecret: String): Boolean {
        return Pattern.matches("^[a-zA-Z0-9]{$CLIENT_KEY_LENGTH}$", clientSecret)
    }
}

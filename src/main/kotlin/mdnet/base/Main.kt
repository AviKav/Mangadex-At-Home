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
package mdnet.base

import ch.qos.logback.classic.LoggerContext
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.regex.Pattern
import kotlin.system.exitProcess
import mdnet.BuildInfo
import mdnet.base.settings.ClientSettings
import org.slf4j.LoggerFactory

object Main {
    private val LOGGER = LoggerFactory.getLogger(Main::class.java)
    private val JACKSON: ObjectMapper = jacksonObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).configure(JsonParser.Feature.ALLOW_COMMENTS, true)

    @JvmStatic
    fun main(args: Array<String>) {
        println(
            "Mangadex@Home Client Version ${BuildInfo.VERSION} (Build ${Constants.CLIENT_BUILD}) initializing"
        )
        println()
        println("Copyright (c) 2020, MangaDex Network")
        println("""
            Mangadex@Home is free software: you can redistribute it and/or modify
            it under the terms of the GNU General Public License as published by
            the Free Software Foundation, either version 3 of the License, or
            (at your option) any later version.

            Mangadex@Home is distributed in the hope that it will be useful,
            but WITHOUT ANY WARRANTY; without even the implied warranty of
            MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
            GNU General Public License for more details.

            You should have received a copy of the GNU General Public License
            along with Mangadex@Home.  If not, see <https://www.gnu.org/licenses/>.
        """.trimIndent())

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

            LOGGER.info { "Client settings loaded: $settings" }
        val client = MangaDexClient(settings)
        Runtime.getRuntime().addShutdownHook(Thread { client.shutdown() })
        client.runLoop()
    }

    fun dieWithError(e: Throwable): Nothing {
            LOGGER.error(e) { "Critical Error" }
        (LoggerFactory.getILoggerFactory() as LoggerContext).stop()
        exitProcess(1)
    }

    fun dieWithError(error: String): Nothing {
        LOGGER.error { "Critical Error: $error" }

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
        if (settings.experimental != null) {
            if (settings.experimental.maxBufferSizeForCacheHit < 0)
            dieWithError("Config Error: Max cache buffer multiple must be >= 0")
        }
    }

    private const val CLIENT_KEY_LENGTH = 52
    private fun isSecretValid(clientSecret: String): Boolean {
        return Pattern.matches("^[a-zA-Z0-9]{$CLIENT_KEY_LENGTH}$", clientSecret)
    }
}

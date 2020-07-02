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

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import mdnet.base.settings.ClientSettings
import mdnet.base.settings.ServerSettings
import org.http4k.client.ApacheClient
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.format.ConfigurableJackson
import org.http4k.format.asConfigurable
import org.http4k.format.withStandardMappings
import org.slf4j.LoggerFactory

import mdnet.base.ServerHandlerJackson.auto
object ServerHandlerJackson : ConfigurableJackson(
    KotlinModule()
    .asConfigurable()
    .withStandardMappings()
    .done()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
)

class ServerHandler(private val settings: ClientSettings) {
    private val client = ApacheClient()

    fun logoutFromControl(): Boolean {
        if (LOGGER.isInfoEnabled) {
            LOGGER.info("Disconnecting from the control server")
        }
        val params = mapOf<String, Any>(
            "secret" to settings.clientSecret
        )

        val request = STRING_ANY_MAP_LENS(params, Request(Method.POST, getServerAddress() + "stop"))
        val response = client(request)

        return response.status.successful
    }

    private fun getPingParams(tlsCreatedAt: String? = null): Map<String, Any> =
        mapOf<String, Any>(
            "secret" to settings.clientSecret,
            "port" to let {
                if (settings.clientExternalPort != 0) {
                    settings.clientExternalPort
                } else {
                    settings.clientPort
                }
            },
            "disk_space" to settings.maxCacheSizeInMebibytes * 1024 * 1024,
            "network_speed" to settings.maxKilobitsPerSecond * 1000 / 8,
            "build_version" to Constants.CLIENT_BUILD
        ).let {
            if (tlsCreatedAt != null) {
                it.plus("tls_created_at" to tlsCreatedAt)
            } else {
                it
            }
        }

    fun loginToControl(): ServerSettings? {
        if (LOGGER.isInfoEnabled) {
            LOGGER.info("Connecting to the control server")
        }

        val request = STRING_ANY_MAP_LENS(getPingParams(), Request(Method.POST, getServerAddress() + "ping"))
        val response = client(request)

        return if (response.status.successful) {
            SERVER_SETTINGS_LENS(response)
        } else {
            null
        }
    }

    fun pingControl(old: ServerSettings): ServerSettings? {
        if (LOGGER.isInfoEnabled) {
            LOGGER.info("Pinging the control server")
        }

        val request = STRING_ANY_MAP_LENS(getPingParams(old.tls!!.createdAt), Request(Method.POST, getServerAddress() + "ping"))
        val response = client(request)

        return if (response.status.successful) {
            SERVER_SETTINGS_LENS(response)
        } else {
            null
        }
    }

    private fun getServerAddress(): String {
        return if (settings.devSettings == null || !settings.devSettings.isDev)
            SERVER_ADDRESS
        else
            SERVER_ADDRESS_DEV
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ServerHandler::class.java)
        private val STRING_ANY_MAP_LENS = Body.auto<Map<String, Any>>().toLens()
        private val SERVER_SETTINGS_LENS = Body.auto<ServerSettings>().toLens()
        private const val SERVER_ADDRESS = "https://api.mangadex.network/"
        private const val SERVER_ADDRESS_DEV = "https://mangadex-test.net/"
    }
}

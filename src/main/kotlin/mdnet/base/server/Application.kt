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
package mdnet.base.server

import java.net.InetAddress
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import mdnet.base.data.Statistics
import mdnet.base.info
import mdnet.base.netty.Netty
import mdnet.base.settings.ClientSettings
import mdnet.base.settings.ServerSettings
import mdnet.cache.DiskLruCache
import org.apache.http.client.config.CookieSpecs
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.HttpClients
import org.http4k.client.ApacheClient
import org.http4k.core.*
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Http4kServer
import org.http4k.server.asServer
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory

private val LOGGER = LoggerFactory.getLogger("Application")

fun getServer(cache: DiskLruCache, serverSettings: ServerSettings, clientSettings: ClientSettings, statistics: AtomicReference<Statistics>, isHandled: AtomicBoolean): Http4kServer {
    val database = Database.connect("jdbc:sqlite:cache/data.db", "org.sqlite.JDBC")
    val client = ApacheClient(responseBodyMode = BodyMode.Stream, client = HttpClients.custom()
        .setSSLHostnameVerifier { hostname, _ ->
            !serverSettings.ignoreImageServerCertCheck && hostname == URL(serverSettings.imageServer).host // Very bad workaround allowing MITM attacks
        }
        .disableConnectionState()
        .setDefaultRequestConfig(
            RequestConfig.custom()
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .setConnectTimeout(3000)
                .setSocketTimeout(3000)
                .setConnectionRequestTimeout(3000)
                .apply {
                    if (clientSettings.clientHostname != "0.0.0.0") {
                        setLocalAddress(InetAddress.getByName(clientSettings.clientHostname))
                    }
                }
                .build())
        .setMaxConnTotal(3000)
        .setMaxConnPerRoute(3000)
        .build())

    val imageServer = ImageServer(cache, database, statistics, serverSettings, client)

    return timeRequest()
        .then(catchAllHideDetails())
        .then(ServerFilters.CatchLensFailure)
        .then(setHandled(isHandled))
        .then(addCommonHeaders())
        .then(
            routes(
                "/data/{chapterHash}/{fileName}" bind Method.GET to imageServer.handler(dataSaver = false),
                "/data-saver/{chapterHash}/{fileName}" bind Method.GET to imageServer.handler(dataSaver = true),
                "/{token}/data/{chapterHash}/{fileName}" bind Method.GET to imageServer.handler(
                    dataSaver = false,
                    tokenized = true
                ),
                "/{token}/data-saver/{chapterHash}/{fileName}" bind Method.GET to imageServer.handler(
                    dataSaver = true,
                    tokenized = true
                )
            )
        )
        .asServer(Netty(serverSettings.tls!!, clientSettings, statistics))
}

fun setHandled(isHandled: AtomicBoolean): Filter {
    return Filter { next: HttpHandler ->
        {
            isHandled.set(true)
            next(it)
        }
    }
}

fun timeRequest(): Filter {
    return Filter { next: HttpHandler ->
        { request: Request ->
            val cleanedUri = request.uri.path.let {
                if (it.startsWith("/data")) {
                    it
                } else {
                    it.replaceBefore("/data", "/{token}")
                }
            }

            LOGGER.info { "Request for $cleanedUri received from ${request.source?.address}" }

            val start = System.currentTimeMillis()
            val response = next(request)
            val latency = System.currentTimeMillis() - start

            LOGGER.info { "Request for $cleanedUri completed (TTFB) in ${latency}ms" }

            response.header("X-Time-Taken", latency.toString())
        }
    }
}

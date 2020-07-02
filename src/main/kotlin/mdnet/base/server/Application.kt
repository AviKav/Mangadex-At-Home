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

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import mdnet.base.data.Statistics
import mdnet.base.netty.Netty
import mdnet.base.settings.ClientSettings
import mdnet.base.settings.ServerSettings
import mdnet.cache.DiskLruCache
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
    val imageServer = ImageServer(cache, statistics, serverSettings, database, isHandled)

    return timeRequest()
            .then(catchAllHideDetails())
            .then(ServerFilters.CatchLensFailure)
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

            if (LOGGER.isInfoEnabled) {
                LOGGER.info("Request for $cleanedUri received from ${request.source?.address}")
            }

            val start = System.currentTimeMillis()
            val response = next(request)
            val latency = System.currentTimeMillis() - start

            if (LOGGER.isInfoEnabled) {
                LOGGER.info("Request for $cleanedUri completed (TTFB) in ${latency}ms")
            }
            response.header("X-Time-Taken", latency.toString())
        }
    }
}

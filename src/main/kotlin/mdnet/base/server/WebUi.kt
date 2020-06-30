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

import java.time.Instant
import java.util.concurrent.atomic.AtomicReference
import mdnet.base.data.Statistics
import mdnet.base.netty.WebUiNetty
import mdnet.base.settings.WebSettings
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.format.Jackson.auto
import org.http4k.routing.ResourceLoader
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.singlePageApp
import org.http4k.server.Http4kServer
import org.http4k.server.asServer

fun getUiServer(
    webSettings: WebSettings,
    statistics: AtomicReference<Statistics>,
    statsMap: Map<Instant, Statistics>
): Http4kServer {
    val statsMapLens = Body.auto<Map<Instant, Statistics>>().toLens()

    return catchAllHideDetails()
        .then(ServerFilters.CatchLensFailure)
        .then(addCommonHeaders())
        .then(
            routes(
                "/api/stats" bind Method.GET to {
                    statsMapLens(mapOf(Instant.now() to statistics.get()), Response(Status.OK))
                },
                "/api/pastStats" bind Method.GET to {
                    synchronized(statsMap) {
                        statsMapLens(statsMap, Response(Status.OK))
                    }
                },
                singlePageApp(ResourceLoader.Classpath("/webui"))
            )
        )
        .asServer(WebUiNetty(webSettings.uiHostname, webSettings.uiPort))
}

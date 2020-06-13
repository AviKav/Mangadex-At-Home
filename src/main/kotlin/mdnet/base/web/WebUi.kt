/* ktlint-disable no-wildcard-imports */
package mdnet.base.web

import mdnet.base.Statistics
import mdnet.base.settings.WebSettings
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.routing.ResourceLoader
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.singlePageApp
import org.http4k.server.Http4kServer
import org.http4k.server.Netty
import org.http4k.server.asServer
import java.util.concurrent.atomic.AtomicReference
import org.http4k.format.Gson.auto
import java.time.Instant

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
        .asServer(Netty(webSettings.uiPort))
}

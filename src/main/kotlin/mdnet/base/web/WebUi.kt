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

fun getUiServer(webSettings: WebSettings, statistics: AtomicReference<Statistics>): Http4kServer {
    val statisticsLens = Body.auto<Statistics>().toLens()

    return catchAllHideDetails()
        .then(ServerFilters.CatchLensFailure)
        .then(addCommonHeaders())
        .then(
            routes(
                "/api/stats" bind Method.GET to {
                    statisticsLens(statistics.get(), Response(Status.OK))
                },
                singlePageApp(ResourceLoader.Classpath("/webui"))
            )
        )
        .asServer(Netty(webSettings.uiPort))
}

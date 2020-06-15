/* ktlint-disable no-wildcard-imports */
package mdnet.base.server

import mdnet.base.Netty
import mdnet.base.ServerSettings
import mdnet.base.Statistics
import mdnet.base.settings.ClientSettings
import mdnet.cache.DiskLruCache
import org.http4k.core.Method
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Http4kServer
import org.http4k.server.asServer
import java.util.concurrent.atomic.AtomicReference

fun getServer(cache: DiskLruCache, serverSettings: ServerSettings, clientSettings: ClientSettings, statistics: AtomicReference<Statistics>): Http4kServer {
    val imageServer = ImageServer(cache, statistics, serverSettings.imageServer)

    return Timer
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
                        dataSaver = false,
                        tokenized = true
                    )
                )
            )
            .asServer(Netty(serverSettings.tls, clientSettings, statistics))
}
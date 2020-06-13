/* ktlint-disable no-wildcard-imports */
package mdnet.base.server

import mdnet.base.Constants
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.slf4j.LoggerFactory
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

private val HTTP_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss O", Locale.ENGLISH)

private val LOGGER = LoggerFactory.getLogger("Application")

fun addCommonHeaders(): Filter {
    return Filter { next: HttpHandler ->
        { request: Request ->
            val response = next(request)
            response.header("Date", HTTP_TIME_FORMATTER.format(ZonedDateTime.now(ZoneOffset.UTC)))
                .header("Server", "Mangadex@Home Node ${Constants.CLIENT_VERSION} (${Constants.CLIENT_BUILD})")
        }
    }
}

fun catchAllHideDetails(): Filter {
    return Filter { next: HttpHandler ->
        { request: Request ->
            try {
                next(request)
            } catch (e: Exception) {
                if (LOGGER.isWarnEnabled) {
                    LOGGER.warn("Request error detected", e)
                }
                Response(Status.INTERNAL_SERVER_ERROR)
            }
        }
    }
}

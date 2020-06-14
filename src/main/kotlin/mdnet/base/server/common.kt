/* ktlint-disable no-wildcard-imports */
package mdnet.base.server

import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import mdnet.base.Constants
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.slf4j.LoggerFactory

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

val Timer = Filter {
    next: HttpHandler -> {
        request: Request ->
            val start = System.currentTimeMillis()
            val response = next(request)
            val latency = System.currentTimeMillis() - start
            if (LOGGER.isTraceEnabled && response.header("X-Uri") != null) {
                // Dirty hack to get sanitizedUri from ImageServer
                val sanitizedUri = response.header("X-Uri")

                // Log in TRACE
                LOGGER.trace("Request for $sanitizedUri completed in ${latency}ms")

                // Delete response header entirely
                response.header("X-Uri", null)
            }
            // Set response header with processing times
            response.header("X-Time-Taken", latency.toString())
    }
}

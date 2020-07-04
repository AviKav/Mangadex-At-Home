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

import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import mdnet.BuildInfo
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
                .header("Server", "Mangadex@Home Node ${BuildInfo.VERSION} (${Constants.CLIENT_BUILD})")
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

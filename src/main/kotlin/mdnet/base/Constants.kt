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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.Duration

object Constants {
    const val CLIENT_BUILD = 13
    const val CLIENT_VERSION = "1.0.0"
    const val WEBUI_VERSION = "0.1.1"
    val MAX_AGE_CACHE: Duration = Duration.ofDays(14)
    val JACKSON: ObjectMapper = jacksonObjectMapper().configure(JsonParser.Feature.ALLOW_COMMENTS, true)
}

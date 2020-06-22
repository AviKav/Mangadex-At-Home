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
package mdnet.base.settings

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import dev.afanasev.sekret.Secret

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class ServerSettings (
    val imageServer: String,
    val latestBuild: Int,
    val url: String,
    val compromised: Boolean,
    val tls: TlsCert?
)

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class TlsCert (
    val createdAt: String,
    @field:Secret val privateKey: String,
    @field:Secret val certificate: String
)

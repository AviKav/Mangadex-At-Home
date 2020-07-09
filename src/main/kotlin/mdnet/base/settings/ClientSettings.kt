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

// client settings are verified correct in Main.kt
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class ClientSettings(
    val maxCacheSizeInMebibytes: Long = 20480,
    val maxMebibytesPerHour: Long = 0,
    val maxKilobitsPerSecond: Long = 0,
    val clientHostname: String = "0.0.0.0",
    val clientPort: Int = 443,
    val clientExternalPort: Int = 0,
    @field:Secret val clientSecret: String = "PASTE-YOUR-SECRET-HERE",
    val threads: Int = 4,
    val gracefulShutdownWaitSeconds: Int = 60,
    val webSettings: WebSettings? = null,
    val devSettings: DevSettings? = null,
    val experimental: Experimental? = null
)

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class WebSettings(
    val uiHostname: String = "127.0.0.1",
    val uiPort: Int = 8080
)

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class DevSettings(
    val isDev: Boolean = false
)

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class Experimental(
    val maxBufferSizeForCacheHit: Int = 0
)

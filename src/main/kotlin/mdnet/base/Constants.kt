package mdnet.base

import java.time.Duration

object Constants {
    const val CLIENT_BUILD = 8
    const val CLIENT_VERSION = "1.0"
    const val WEBUI_VERSION = "0.0.4"
    val MAX_AGE_CACHE: Duration = Duration.ofDays(14)
}

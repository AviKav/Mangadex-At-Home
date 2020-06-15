package mdnet.base

import java.time.Duration

object Constants {
    const val CLIENT_BUILD = 7
    const val CLIENT_VERSION = "1.0"
    val MAX_AGE_CACHE: Duration = Duration.ofDays(14)
}

package mdnet.base

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.time.Duration

object Constants {
    const val CLIENT_BUILD = 8
    const val CLIENT_VERSION = "1.0"
    val MAX_AGE_CACHE: Duration = Duration.ofDays(14)
    @JvmField
    val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
}

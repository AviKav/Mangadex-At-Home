package mdnet.base

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.time.Duration

object Constants {
    const val CLIENT_BUILD = 9
    const val CLIENT_VERSION = "1.0"
    const val WEBUI_VERSION = "0.1.1"
    val MAX_AGE_CACHE: Duration = Duration.ofDays(14)
    @JvmField
    val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
}

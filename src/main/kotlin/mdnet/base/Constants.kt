package mdnet.base

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.Duration

object Constants {
    const val CLIENT_BUILD = 12
    const val CLIENT_VERSION = "1.0"
    const val WEBUI_VERSION = "0.1.1"
    val MAX_AGE_CACHE: Duration = Duration.ofDays(14)
    val JACKSON: ObjectMapper = jacksonObjectMapper().configure(JsonParser.Feature.ALLOW_COMMENTS, true)
}

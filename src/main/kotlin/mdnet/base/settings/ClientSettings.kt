package mdnet.base.settings

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import dev.afanasev.sekret.Secret

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class ClientSettings(
    val maxCacheSizeInMebibytes: Long = 20480,
    val maxMebibytesPerHour: Long = 0,
    val maxKilobitsPerSecond: Long = 0,
    val clientHostname: String = "0.0.0.0",
    val clientPort: Int = 443,
    @field:Secret val clientSecret: String = "PASTE-YOUR-SECRET-HERE",
    val threads: Int = 4,
    val gracefulShutdownWaitSeconds: Int = 60,
    val webSettings: WebSettings? = null
)

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class WebSettings(
    val uiHostname: String = "127.0.0.1",
    val uiPort: Int = 8080
)

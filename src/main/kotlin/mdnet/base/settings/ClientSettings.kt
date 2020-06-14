package mdnet.base.settings

import com.google.gson.annotations.SerializedName
import dev.afanasev.sekret.Secret

data class ClientSettings(
    @field:SerializedName("max_cache_size_mib") val maxCacheSizeMib: Long = 20480,
    @field:SerializedName("max_bandwidth_mib_per_hour") val maxBandwidthMibPerHour: Long = 0,
    @field:SerializedName("max_burst_rate_kib_per_second") val maxBurstRateKibPerSecond: Long = 0,
    @field:SerializedName("client_hostname") val clientHostname: String = "0.0.0.0",
    @field:SerializedName("client_port") val clientPort: Int = 443,
    @field:Secret @field:SerializedName("client_secret") val clientSecret: String = "PASTE-YOUR-SECRET-HERE",
    @field:SerializedName("threads") val threads: Int = 32,
    @field:SerializedName("web_settings") val webSettings: WebSettings? = null
)

data class WebSettings(
    @field:SerializedName("ui_hostname") val uiHostname: String = "127.0.0.1",
    @field:SerializedName("ui_port") val uiPort: Int = 8080
)

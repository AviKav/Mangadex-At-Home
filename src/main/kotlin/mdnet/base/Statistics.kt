package mdnet.base

import com.google.gson.annotations.SerializedName

data class Statistics(
    @field:SerializedName("requests_served") val requestsServed: Int = 0,
    @field:SerializedName("cache_hits") val cacheHits: Int = 0,
    @field:SerializedName("cache_misses") val cacheMisses: Int = 0,
    @field:SerializedName("browser_cached") val browserCached: Int = 0,
    @field:SerializedName("bytes_sent") val bytesSent: Long = 0
)

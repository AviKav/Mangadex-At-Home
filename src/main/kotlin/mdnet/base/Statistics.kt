package mdnet.base

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class Statistics(
    val requestsServed: Int = 0,
    val cacheHits: Int = 0,
    val cacheMisses: Int = 0,
    val browserCached: Int = 0,
    val bytesSent: Long = 0,
    val bytesOnDisk: Long = 0
)

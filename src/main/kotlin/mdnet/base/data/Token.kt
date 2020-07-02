package mdnet.base.data

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.OffsetDateTime

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class Token(val expires: OffsetDateTime, val ip: String, val hash: String, val clientId: String)

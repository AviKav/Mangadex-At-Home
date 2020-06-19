package mdnet.base.settings

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import dev.afanasev.sekret.Secret

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class ServerSettings (
    val imageServer: String,
    val latestBuild: Int,
    val url: String,
    val compromised: Boolean,
    val tls: TlsCert?
)

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class TlsCert (
    val createdAt: String,
    @field:Secret val privateKey: String,
    @field:Secret val certificate: String
)

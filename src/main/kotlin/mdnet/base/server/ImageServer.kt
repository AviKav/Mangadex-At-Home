/*
Mangadex@Home
Copyright (c) 2020, MangaDex Network
This file is part of MangaDex@Home.

MangaDex@Home is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

MangaDex@Home is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this MangaDex@Home.  If not, see <http://www.gnu.org/licenses/>.
 */
/* ktlint-disable no-wildcard-imports */
package mdnet.base.server

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.goterl.lazycode.lazysodium.LazySodiumJava
import com.goterl.lazycode.lazysodium.SodiumJava
import com.goterl.lazycode.lazysodium.exceptions.SodiumException
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.SecretKeySpec
import mdnet.base.Constants
import mdnet.base.data.ImageData
import mdnet.base.data.ImageDatum
import mdnet.base.data.Statistics
import mdnet.base.data.Token
import mdnet.base.info
import mdnet.base.settings.ClientSettings
import mdnet.base.settings.ServerSettings
import mdnet.base.trace
import mdnet.base.warn
import mdnet.cache.CachingInputStream
import mdnet.cache.DiskLruCache
import org.http4k.core.*
import org.http4k.filter.CachingFilters
import org.http4k.lens.Path
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

class ImageServer(
    private val cache: DiskLruCache,
    private val database: Database,
    private val statistics: AtomicReference<Statistics>,
    private val serverSettings: ServerSettings,
    private val clientSettings: ClientSettings,
    private val client: HttpHandler
) {
    init {
        transaction(database) {
            SchemaUtils.create(ImageData)
        }
    }
    private val executor = Executors.newCachedThreadPool()
    private val maxBufferSizeForCacheHit: Int? = clientSettings.experimental?.maxBufferSizeForCacheHit
            ?.takeUnless { it == 0 }
            ?.times(8 * 1024)

    fun handler(dataSaver: Boolean, tokenized: Boolean = false): HttpHandler {
        val sodium = LazySodiumJava(SodiumJava())

        return baseHandler().then { request ->
            val chapterHash = Path.of("chapterHash")(request)
            val fileName = Path.of("fileName")(request)

            val sanitizedUri = if (dataSaver) {
                "/data-saver"
            } else {
                "/data"
            } + "/$chapterHash/$fileName"

            if (!request.referrerMatches(ALLOWED_REFERER_DOMAINS)) {
                LOGGER.info { "Request for $sanitizedUri rejected due to non-allowed referrer ${request.header("Referer")}" }
                return@then Response(Status.FORBIDDEN)
            }

            if (tokenized || serverSettings.forceTokens) {
                val tokenArr = Base64.getUrlDecoder().decode(Path.of("token")(request))
                val token = try {
                    JACKSON.readValue<Token>(
                        try {
                            sodium.cryptoBoxOpenEasyAfterNm(
                                tokenArr.sliceArray(24 until tokenArr.size), tokenArr.sliceArray(0 until 24), serverSettings.tokenKey
                            )
                        } catch (_: SodiumException) {
                            LOGGER.info { "Request for $sanitizedUri rejected for invalid token" }
                            return@then Response(Status.FORBIDDEN)
                        }
                    )
                } catch (e: JsonProcessingException) {
                    LOGGER.info { "Request for $sanitizedUri rejected for invalid token" }
                    return@then Response(Status.FORBIDDEN)
                }

                if (OffsetDateTime.now().isAfter(token.expires)) {
                    LOGGER.info { "Request for $sanitizedUri rejected for expired token" }
                    return@then Response(Status.GONE)
                }

                if (token.hash != chapterHash) {
                    LOGGER.info { "Request for $sanitizedUri rejected for inapplicable token" }
                    return@then Response(Status.FORBIDDEN)
                }
            }

            statistics.getAndUpdate {
                it.copy(requestsServed = it.requestsServed + 1)
            }

            val rc4Bytes = if (dataSaver) {
                md5Bytes("saver$chapterHash.$fileName")
            } else {
                md5Bytes("$chapterHash.$fileName")
            }
            val imageId = printHexString(rc4Bytes)

            val snapshot = cache.getUnsafe(imageId.toCacheId())
            val imageDatum = synchronized(database) {
                transaction(database) {
                    ImageDatum.findById(imageId)
                }
            }

            if (snapshot != null && imageDatum != null) {
                request.handleCacheHit(sanitizedUri, getRc4(rc4Bytes), snapshot, imageDatum)
            } else {
                if (snapshot != null) {
                    snapshot.close()
                    LOGGER.warn { "Removing cache file for $sanitizedUri without corresponding DB entry" }
                    cache.removeUnsafe(imageId.toCacheId())
                }

                request.handleCacheMiss(sanitizedUri, getRc4(rc4Bytes), imageId, imageDatum)
            }
        }
    }

    /**
     * Filters referrers based on passed (sub)domains. Ignores `scheme` (protocol) in URL
     */
    private fun Request.referrerMatches(allowedDomains: List<String>, permitBlank: Boolean = true): Boolean {
        val referer = this.header("Referer") ?: return permitBlank // Referrer was misspelled as "Referer" and now we're stuck with it -_-
        if (referer == "") return permitBlank

        return allowedDomains.any {
            referer.substringAfter("//") // Ignore scheme
                    .substringBefore("/") // Ignore path
                    .substringBefore(":")
                    .endsWith(it)
        }
    }

    private fun Request.handleCacheHit(sanitizedUri: String, cipher: Cipher, snapshot: DiskLruCache.Snapshot, imageDatum: ImageDatum): Response {
        // our files never change, so it's safe to use the browser cache
        return if (this.header("If-Modified-Since") != null) {
            statistics.getAndUpdate {
                it.copy(browserCached = it.browserCached + 1)
            }

            LOGGER.info { "Request for $sanitizedUri cached by browser" }

            val lastModified = imageDatum.lastModified
            snapshot.close()

            Response(Status.NOT_MODIFIED)
                .header("Last-Modified", lastModified)
        } else {
            statistics.getAndUpdate {
                it.copy(cacheHits = it.cacheHits + 1)
            }

            LOGGER.info { "Request for $sanitizedUri hit cache" }
            val cacheStream = snapshot.getInputStream(0)
            val bufferSize = maxBufferSizeForCacheHit?.coerceAtMost(snapshot.getLength(0).toInt())
            val bufferedStream = bufferSize?.let {
                BufferedInputStream(cacheStream, bufferSize)
            } ?: BufferedInputStream(cacheStream) // Todo: Move into builder. It's untidy having the null propagate all the way here but I'm tired and tomorrow is a fast day.

            respondWithImage(
                    CipherInputStream(bufferedStream, cipher),
                    snapshot.getLength(0).toString(), imageDatum.contentType, imageDatum.lastModified,
                    true
            )
        }
    }

    private fun Request.handleCacheMiss(sanitizedUri: String, cipher: Cipher, imageId: String, imageDatum: ImageDatum?): Response {
        LOGGER.info { "Request for $sanitizedUri missed cache" }

        statistics.getAndUpdate {
            it.copy(cacheMisses = it.cacheMisses + 1)
        }

        val mdResponse = client(Request(Method.GET, "${serverSettings.imageServer}$sanitizedUri"))

        if (mdResponse.status != Status.OK) {
            LOGGER.trace { "Upstream query for $sanitizedUri errored with status ${mdResponse.status}" }

            mdResponse.close()
            return Response(mdResponse.status)
        }

        LOGGER.trace { "Upstream query for $sanitizedUri succeeded" }

        val contentType = mdResponse.header("Content-Type")!!
        val contentLength = mdResponse.header("Content-Length")
        val lastModified = mdResponse.header("Last-Modified")

        val editor = cache.editUnsafe(imageId.toCacheId())

        // A null editor means that this file is being written to
        // concurrently so we skip the cache process
        return if (editor != null && contentLength != null && lastModified != null) {
            LOGGER.trace { "Request for $sanitizedUri is being cached and served" }

            if (imageDatum == null) {
                synchronized(database) {
                    transaction(database) {
                        ImageDatum.new(imageId) {
                            this.contentType = contentType
                            this.lastModified = lastModified
                        }
                    }
                }
            }

            val tee = CachingInputStream(
                mdResponse.body.stream,
                executor, CipherOutputStream(BufferedOutputStream(editor.newOutputStream(0)), cipher)
            ) {
                try {
                    if (editor.getLength(0) == contentLength.toLong()) {
                        LOGGER.info { "Cache download for $sanitizedUri committed" }
                        editor.commit()
                    } else {
                        LOGGER.warn { "Cache download for $sanitizedUri aborted" }
                        editor.abort()
                    }
                } catch (e: Exception) {
                    LOGGER.warn(e) { "Cache go/no go for $sanitizedUri failed" }
                }
            }
            respondWithImage(tee, contentLength, contentType, lastModified, false)
        } else {
            editor?.abort()
            LOGGER.trace { "Request for $sanitizedUri is being served" }
            respondWithImage(mdResponse.body.stream, contentLength, contentType, lastModified, false)
        }
    }

    private fun String.toCacheId() =
        this.substring(0, 8).replace("..(?!$)".toRegex(), "$0 ").split(" ".toRegex())
            .plus(this).joinToString(File.separator)

    private fun respondWithImage(input: InputStream, length: String?, type: String, lastModified: String?, cached: Boolean): Response =
        Response(Status.OK)
            .header("Content-Type", type)
            .header("X-Content-Type-Options", "nosniff")
            .let {
                if (length != null) {
                    it.body(input, length.toLong()).header("Content-Length", length)
                } else {
                    it.body(input).header("Transfer-Encoding", "chunked")
                }
            }
            .let {
                if (lastModified != null) {
                    it.header("Last-Modified", lastModified)
                } else {
                    it
                }
            }
            .header("X-Cache", if (cached) "HIT" else "MISS")

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ImageServer::class.java)
        private val JACKSON: ObjectMapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(JavaTimeModule())
        private val ALLOWED_REFERER_DOMAINS = listOf("mangadex.org", "mangadex.network") // TODO: Factor out hardcoded domains?

        private fun baseHandler(): Filter =
            CachingFilters.Response.MaxAge(Clock.systemUTC(), Constants.MAX_AGE_CACHE)
                .then(Filter { next: HttpHandler ->
                    { request: Request ->
                        val response = next(request)
                        response.header("access-control-allow-origin", "https://mangadex.org")
                        .header("access-control-allow-headers", "*")
                        .header("access-control-allow-methods", "GET")
                        .header("timing-allow-origin", "https://mangadex.org")
                    }
                })
    }
}

private fun getRc4(key: ByteArray): Cipher {
    val rc4 = Cipher.getInstance("RC4")
    rc4.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "RC4"))
    return rc4
}

private fun md5Bytes(stringToHash: String): ByteArray {
    val digest = MessageDigest.getInstance("MD5")
    return digest.digest(stringToHash.toByteArray())
}

private fun printHexString(bytes: ByteArray): String {
    val sb = StringBuilder()
    for (b in bytes) {
        sb.append(String.format("%02x", b))
    }
    return sb.toString()
}

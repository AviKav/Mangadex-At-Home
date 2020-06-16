/* ktlint-disable no-wildcard-imports */
package mdnet.base.server

import mdnet.base.Constants
import mdnet.base.Statistics
import mdnet.base.dao.ImageData
import mdnet.base.dao.ImageDatum
import mdnet.cache.CachingInputStream
import mdnet.cache.DiskLruCache
import org.apache.http.client.config.CookieSpecs
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.HttpClients
import org.http4k.client.ApacheClient
import org.http4k.core.*
import org.http4k.filter.MaxAgeTtl
import org.http4k.lens.Path
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.SecretKeySpec

private const val THREADS_TO_ALLOCATE = 262144 // 2**18 // Honestly, no reason to not just let 'er rip. Inactive connections will expire on their own :D
private val LOGGER = LoggerFactory.getLogger(ImageServer::class.java)

class ImageServer(private val cache: DiskLruCache, private val statistics: AtomicReference<Statistics>, private val upstreamUrl: String, private val database: Database) {
    init {
        transaction(database) {
            SchemaUtils.create(ImageData)
        }
    }
    private val executor = Executors.newCachedThreadPool()
    private val client = ApacheClient(responseBodyMode = BodyMode.Stream, client = HttpClients.custom()
        .setDefaultRequestConfig(
            RequestConfig.custom()
            .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
            .setConnectTimeout(3000)
            .setSocketTimeout(3000)
            .setConnectionRequestTimeout(3000)
            .build())
        .setMaxConnTotal(THREADS_TO_ALLOCATE)
        .setMaxConnPerRoute(THREADS_TO_ALLOCATE)
        .build())

    fun handler(dataSaver: Boolean, tokenized: Boolean = false): HttpHandler = { request ->
        val chapterHash = Path.of("chapterHash")(request)
        val fileName = Path.of("fileName")(request)

        val sanitizedUri = if (dataSaver) {
            "/data-saver"
        } else {
            "/data"
        } + "/$chapterHash/$fileName"

        if (LOGGER.isInfoEnabled) {
            LOGGER.info("Request for $sanitizedUri received")
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
                .header("X-Uri", sanitizedUri)
        } else {
            if (snapshot != null) {
                snapshot.close()
                if (LOGGER.isWarnEnabled) {
                    LOGGER.warn("Removing cache file for $sanitizedUri without corresponding DB entry")
                }
                cache.removeUnsafe(imageId.toCacheId())
            }
            if (imageDatum != null) {
                if (LOGGER.isWarnEnabled) {
                    LOGGER.warn("Deleting DB entry for $sanitizedUri without corresponding file")
                }
                synchronized(database) {
                    transaction(database) {
                        imageDatum.delete()
                    }
                }
            }

            request.handleCacheMiss(sanitizedUri, getRc4(rc4Bytes), imageId)
                .header("X-Uri", sanitizedUri)
        }
    }

    private fun Request.handleCacheHit(sanitizedUri: String, cipher: Cipher, snapshot: DiskLruCache.Snapshot, imageDatum: ImageDatum): Response {
        // our files never change, so it's safe to use the browser cache
        return if (this.header("If-Modified-Since") != null) {
            statistics.getAndUpdate {
                it.copy(browserCached = it.browserCached + 1)
            }

            if (LOGGER.isInfoEnabled) {
                LOGGER.info("Request for $sanitizedUri cached by browser")
            }

            val lastModified = snapshot.getString(2)
            snapshot.close()

            Response(Status.NOT_MODIFIED)
                .header("Last-Modified", lastModified)
        } else {
            statistics.getAndUpdate {
                it.copy(cacheHits = it.cacheHits + 1)
            }

            if (LOGGER.isInfoEnabled) {
                LOGGER.info("Request for $sanitizedUri hit cache")
            }

            respondWithImage(
                CipherInputStream(BufferedInputStream(snapshot.getInputStream(0)), cipher),
                snapshot.getLength(0).toString(), imageDatum.contentType, imageDatum.lastModified,
                true
            )
        }
    }

    private fun Request.handleCacheMiss(sanitizedUri: String, cipher: Cipher, imageId: String): Response {
        if (LOGGER.isInfoEnabled) {
            LOGGER.info("Request for $sanitizedUri missed cache")
        }
        statistics.getAndUpdate {
            it.copy(cacheMisses = it.cacheMisses + 1)
        }

        val mdResponse = client(Request(Method.GET, "$upstreamUrl$sanitizedUri"))

        if (mdResponse.status != Status.OK) {
            if (LOGGER.isTraceEnabled) {
                LOGGER.trace("Upstream query for $sanitizedUri errored with status {}", mdResponse.status)
            }
            mdResponse.close()
            return Response(mdResponse.status)
        }

        if (LOGGER.isTraceEnabled) {
            LOGGER.trace("Upstream query for $sanitizedUri succeeded")
        }

        val contentType = mdResponse.header("Content-Type")!!
        val contentLength = mdResponse.header("Content-Length")
        val lastModified = mdResponse.header("Last-Modified")

        val editor = cache.editUnsafe(imageId.toCacheId())

        // A null editor means that this file is being written to
        // concurrently so we skip the cache process
        return if (editor != null && contentLength != null && lastModified != null) {
            if (LOGGER.isTraceEnabled) {
                LOGGER.trace("Request for $sanitizedUri is being cached and served")
            }

            synchronized(database) {
                transaction(database) {
                    ImageDatum.new(imageId) {
                        this.contentType = contentType
                        this.lastModified = lastModified
                    }
                }
            }

            val tee = CachingInputStream(
                mdResponse.body.stream,
                executor, CipherOutputStream(BufferedOutputStream(editor.newOutputStream(0)), cipher)
            ) {
                try {
                    if (editor.getLength(0) == contentLength.toLong()) {
                        if (LOGGER.isInfoEnabled) {
                            LOGGER.info("Cache download for $sanitizedUri committed")
                        }
                        editor.commit()
                    } else {
                        if (LOGGER.isInfoEnabled) {
                            LOGGER.info("Cache download for $sanitizedUri aborted")
                        }
                        editor.abort()
                    }
                } catch (e: Exception) {
                    if (LOGGER.isWarnEnabled) {
                        LOGGER.warn("Cache go/no go for $sanitizedUri failed", e)
                    }
                }
            }
            respondWithImage(tee, contentLength, contentType, lastModified, false)
        } else {
            editor?.abort()

            if (LOGGER.isTraceEnabled) {
                LOGGER.trace("Request for $sanitizedUri is being served")
            }
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
            .header(
                "Cache-Control",
                listOf("public", MaxAgeTtl(Constants.MAX_AGE_CACHE).toHeaderValue()).joinToString(", ")
            )
            .header("Access-Control-Allow-Origin", "https://mangadex.org")
            .header("Access-Control-Expose-Headers", "*")
            .header("Timing-Allow-Origin", "https://mangadex.org")
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

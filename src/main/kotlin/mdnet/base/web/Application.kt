/* ktlint-disable no-wildcard-imports */
package mdnet.base.web

import mdnet.base.Constants
import mdnet.base.Netty
import mdnet.base.ServerSettings
import mdnet.base.Statistics
import mdnet.base.settings.ClientSettings
import mdnet.cache.CachingInputStream
import mdnet.cache.DiskLruCache
import org.apache.http.client.config.CookieSpecs
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.HttpClients
import org.http4k.client.ApacheClient
import org.http4k.core.BodyMode
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.filter.CachingFilters
import org.http4k.filter.MaxAgeTtl
import org.http4k.filter.ServerFilters
import org.http4k.lens.Path
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Http4kServer
import org.http4k.server.asServer
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.SecretKeySpec

private val LOGGER = LoggerFactory.getLogger("Application")
private const val THREADS_TO_ALLOCATE = 262144 // 2**18 // Honestly, no reason to not just let 'er rip. Inactive connections will expire on their own :D

fun getServer(cache: DiskLruCache, serverSettings: ServerSettings, clientSettings: ClientSettings, statistics: AtomicReference<Statistics>): Http4kServer {
    val executor = Executors.newCachedThreadPool()

    if (LOGGER.isInfoEnabled) {
        LOGGER.info("Starting image retriever")
    }

    val client = ApacheClient(responseBodyMode = BodyMode.Stream, client = HttpClients.custom()
            .setDefaultRequestConfig(RequestConfig.custom()
                    .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                    .setConnectTimeout(3000)
                    .setSocketTimeout(3000)
                    .setConnectionRequestTimeout(3000)
                    .build())
            .setMaxConnTotal(THREADS_TO_ALLOCATE)
            .setMaxConnPerRoute(THREADS_TO_ALLOCATE)
            .build())

    val app = { dataSaver: Boolean ->
        { request: Request ->
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

            val rc4Bytes = if (dataSaver) {
                md5Bytes("saver$chapterHash.$fileName")
            } else {
                md5Bytes("$chapterHash.$fileName")
            }
            val cacheId = printHexString(rc4Bytes)
            statistics.getAndUpdate {
                it.copy(requestsServed = it.requestsServed + 1)
            }

            // Netty doesn't do Content-Length or Content-Type, so we have the pleasure of doing that ourselves
            fun respondWithImage(input: InputStream, length: String?, type: String, lastModified: String?, cached: Boolean): Response =
                    Response(Status.OK)
                            .header("Content-Type", type)
                            .header("X-Content-Type-Options", "nosniff")
                            .header(
                                    "Cache-Control",
                                    listOf("public", MaxAgeTtl(Constants.MAX_AGE_CACHE).toHeaderValue()).joinToString(", ")
                            )
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
                            .let {
                                if (cached) {
                                    it.header("X-Cache", "HIT")
                                } else {
                                    it.header("X-Cache", "MISS")
                                }
                            }

            val snapshot = cache.get(cacheId)
            if (snapshot != null) {
                // our files never change, so it's safe to use the browser cache
                if (request.header("If-Modified-Since") != null) {
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
                            CipherInputStream(BufferedInputStream(snapshot.getInputStream(0)), getRc4(rc4Bytes)),
                            snapshot.getLength(0).toString(), snapshot.getString(1), snapshot.getString(2),
                        true
                    )
                }
            } else {
                statistics.getAndUpdate {
                    it.copy(cacheMisses = it.cacheMisses + 1)
                }

                if (LOGGER.isInfoEnabled) {
                    LOGGER.info("Request for $sanitizedUri missed cache")
                }
                val mdResponse = client(Request(Method.GET, "${serverSettings.imageServer}$sanitizedUri"))

                if (mdResponse.status != Status.OK) {
                    if (LOGGER.isTraceEnabled) {
                        LOGGER.trace("Upstream query for $sanitizedUri errored with status {}", mdResponse.status)
                    }
                    mdResponse.close()
                    Response(mdResponse.status)
                } else {
                    if (LOGGER.isTraceEnabled) {
                        LOGGER.trace("Upstream query for $sanitizedUri succeeded")
                    }

                    val contentType = mdResponse.header("Content-Type")!!
                    val contentLength = mdResponse.header("Content-Length")
                    val lastModified = mdResponse.header("Last-Modified")

                    val editor = cache.edit(cacheId)

                    // A null editor means that this file is being written to
                    // concurrently so we skip the cache process
                    if (editor != null && contentLength != null && lastModified != null) {
                        if (LOGGER.isTraceEnabled) {
                            LOGGER.trace("Request for $sanitizedUri is being cached and served")
                        }
                        editor.setString(1, contentType)
                        editor.setString(2, lastModified)

                        val tee = CachingInputStream(
                                mdResponse.body.stream,
                                executor, CipherOutputStream(BufferedOutputStream(editor.newOutputStream(0)), getRc4(rc4Bytes))
                        ) {
                            // Note: if neither of the options get called/are in the log
                            // check that tee gets closed and for exceptions in this lambda
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
            }
        }
    }

    CachingFilters

    return catchAllHideDetails()
            .then(ServerFilters.CatchLensFailure)
            .then(addCommonHeaders())
            .then(
                    routes(
                            "/data/{chapterHash}/{fileName}" bind Method.GET to app(false),
                            "/data-saver/{chapterHash}/{fileName}" bind Method.GET to app(true),
                            "/{token}/data/{chapterHash}/{fileName}" bind Method.GET to app(false),
                            "/{token}/data-saver/{chapterHash}/{fileName}" bind Method.GET to app(true)
                    )
            )
            .asServer(Netty(serverSettings.tls, clientSettings, statistics))
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
/* ktlint-disable no-wildcard-imports */
package mdnet.base

import mdnet.cache.DiskLruCache
import org.apache.http.client.config.CookieSpecs
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.HttpClients
import org.http4k.client.ApacheClient
import org.http4k.core.BodyMode
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.filter.MaxAgeTtl
import org.http4k.filter.ServerFilters
import org.http4k.lens.Path
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Http4kServer
import org.http4k.server.asServer
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.security.MessageDigest
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.SecretKeySpec

private val LOGGER = LoggerFactory.getLogger("Application")

fun getServer(cache: DiskLruCache, serverSettings: ServerSettings, clientSettings: ClientSettings, statistics: AtomicReference<Statistics>): Http4kServer {
    val executor = Executors.newCachedThreadPool()

    val client = ApacheClient(responseBodyMode = BodyMode.Stream, client = HttpClients.custom()
        .setDefaultRequestConfig(RequestConfig.custom()
            .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
            .build())
        .build())

    val app = { request: Request ->

        val chapterHash = Path.of("chapterHash")(request)
        val fileName = Path.of("fileName")(request)
        val cacheId = md5String("$chapterHash.$fileName")

        statistics.get().requestsServed.incrementAndGet()

        // Netty doesn't do Content-Length or Content-Type, so we have the pleasure of doing that ourselves
        fun respond(input: InputStream, length: String, type: String): Response =
            Response(Status.OK).header("Content-Length", length)
                .header("Content-Type", type)
                .header("X-Content-Type-Options", "nosniff")
                .body(input, length.toLong())

        val snapshot = cache.get(cacheId)
        if (snapshot != null) {
            statistics.get().cacheHits.incrementAndGet()
            if (LOGGER.isTraceEnabled) {
                LOGGER.trace("Request for $chapterHash/$fileName hit cache")
            }

            respond(CipherInputStream(snapshot.getInputStream(0), getRc4(cacheId)),
                snapshot.getLength(0).toString(), snapshot.getString(1))
        } else {
            statistics.get().cacheMisses.incrementAndGet()
            if (LOGGER.isTraceEnabled) {
                LOGGER.trace("Request for $chapterHash/$fileName missed cache")
            }
            val mdResponse = client(Request(Method.GET, "${serverSettings.imageServer}${request.uri}"))

            if (mdResponse.status != Status.OK) {
                if (LOGGER.isTraceEnabled) {
                    LOGGER.trace("Request for $chapterHash/$fileName errored with status {}", mdResponse.status)
                }
                mdResponse.close()
                Response(mdResponse.status)
            } else {
                val contentLength = mdResponse.header("Content-Length")!!
                val contentType = mdResponse.header("Content-Type")!!

                val editor = cache.edit(cacheId)

                // A null editor means that this file is being written to
                // concurrently so we skip the cache process
                if (editor != null) {
                    if (LOGGER.isTraceEnabled) {
                        LOGGER.trace("Request for $chapterHash/$fileName is being cached and served")
                    }
                    editor.setString(1, contentType)

                    val tee = CachingInputStream(mdResponse.body.stream,
                        executor, CipherOutputStream(editor.newOutputStream(0), getRc4(cacheId))) {
                        // Note: if neither of the options get called/are in the log
                        // check that tee gets closed and for exceptions in this lambda
                        if (editor.getLength(0) == contentLength.toLong()) {
                            if (LOGGER.isTraceEnabled) {
                                LOGGER.trace("Cache download $chapterHash/$fileName committed")
                            }

                            editor.commit()
                        } else {
                            if (LOGGER.isTraceEnabled) {
                                LOGGER.trace("Cache download $chapterHash/$fileName aborted")
                            }

                            editor.abort()
                        }
                    }
                    respond(tee, contentLength, contentType)
                } else {
                    if (LOGGER.isTraceEnabled) {
                        LOGGER.trace("Request for $chapterHash/$fileName is being served")
                    }

                    respond(mdResponse.body.stream, contentLength, contentType)
                }
            }
        }
    }

    return catchAllHideDetails()
        .then(ServerFilters.CatchLensFailure)
        .then(addCommonHeaders())
        .then(
            routes(
                "/data/{chapterHash}/{fileName}" bind Method.GET to app
            )
        )
        .asServer(Netty(serverSettings.tls, clientSettings, statistics))
}

private fun getRc4(key: String): Cipher {
    val rc4 = Cipher.getInstance("RC4")
    rc4.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key.toByteArray(), "RC4"))
    return rc4
}

private val HTTP_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss O", Locale.ENGLISH)

private fun addCommonHeaders(): Filter {
    return Filter { next: HttpHandler ->
        { request: Request ->
            val now = ZonedDateTime.now(ZoneOffset.UTC)
            val response = next(request)
            response.header("Date", HTTP_TIME_FORMATTER.format(now))
                .header("Server", "Mangadex@Home Node")
                .header("Cache-Control", listOf("public", MaxAgeTtl(Constants.MAX_AGE_CACHE).toHeaderValue()).joinToString(", "))
                .header("Expires", HTTP_TIME_FORMATTER.format(now.plusSeconds(Constants.MAX_AGE_CACHE.seconds)))
                .header("Cache-Control", "public, max-age=604800") // 1 week browser cache
                .header("Timing-Allow-Origin", "https://mangadex.org")
        }
    }
}

private fun catchAllHideDetails(): Filter {
    return Filter { next: HttpHandler ->
        { request: Request ->
            try {
                next(request)
            } catch (e: Exception) {
                Response(Status.INTERNAL_SERVER_ERROR)
            }
        }
    }
}

private fun md5String(stringToHash: String): String {
    val digest = MessageDigest.getInstance("MD5")

    val sb = StringBuilder()
    for (b in digest.digest(stringToHash.toByteArray())) {
        sb.append(String.format("%02x", b))
    }
    return sb.toString()
}

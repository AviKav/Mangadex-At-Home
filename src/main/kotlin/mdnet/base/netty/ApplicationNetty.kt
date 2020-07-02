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
package mdnet.base.netty

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.http.*
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.stream.ChunkedWriteHandler
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import io.netty.handler.traffic.GlobalTrafficShapingHandler
import io.netty.handler.traffic.TrafficCounter
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.SocketException
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.net.ssl.SSLException
import mdnet.base.Constants
import mdnet.base.data.Statistics
import mdnet.base.settings.ClientSettings
import mdnet.base.settings.TlsCert
import org.http4k.core.HttpHandler
import org.http4k.server.Http4kChannelHandler
import org.http4k.server.Http4kServer
import org.http4k.server.ServerConfig
import org.slf4j.LoggerFactory

private val LOGGER = LoggerFactory.getLogger("Application")

class Netty(private val tls: TlsCert, private val clientSettings: ClientSettings, private val statistics: AtomicReference<Statistics>) : ServerConfig {
    override fun toServer(httpHandler: HttpHandler): Http4kServer = object : Http4kServer {
        private val masterGroup = NioEventLoopGroup(clientSettings.threads)
        private val workerGroup = NioEventLoopGroup(clientSettings.threads)
        private lateinit var closeFuture: ChannelFuture
        private lateinit var address: InetSocketAddress

        private val burstLimiter = object : GlobalTrafficShapingHandler(
                workerGroup, clientSettings.maxKilobitsPerSecond * 1000L / 8L, 0, 50) {
            override fun doAccounting(counter: TrafficCounter) {
                statistics.getAndUpdate {
                    it.copy(bytesSent = it.bytesSent + counter.cumulativeWrittenBytes())
                }
                counter.resetCumulativeTime()
            }
        }

        override fun start(): Http4kServer = apply {
            if (LOGGER.isInfoEnabled) {
                LOGGER.info("Starting Netty with {} threads", clientSettings.threads)
            }

            val certs = getX509Certs(tls.certificate)
            val sslContext = SslContextBuilder
                    .forServer(getPrivateKey(tls.privateKey), certs)
                    .protocols("TLSv1.3", "TLSv1.2", "TLSv1.1", "TLSv1")
                    .build()

            val bootstrap = ServerBootstrap()
            bootstrap.group(masterGroup, workerGroup)
                    .channelFactory(ChannelFactory<ServerChannel> { NioServerSocketChannel() })
                    .childHandler(object : ChannelInitializer<SocketChannel>() {
                        public override fun initChannel(ch: SocketChannel) {
                            ch.pipeline().addLast("ssl", sslContext.newHandler(ch.alloc()))

                            ch.pipeline().addLast("codec", HttpServerCodec())
                            ch.pipeline().addLast("keepAlive", HttpServerKeepAliveHandler())
                            ch.pipeline().addLast("aggregator", HttpObjectAggregator(65536))

                            ch.pipeline().addLast("burstLimiter", burstLimiter)

                            ch.pipeline().addLast("readTimeoutHandler", ReadTimeoutHandler(Constants.MAX_READ_TIME_SECONDS))
                            ch.pipeline().addLast("writeTimeoutHandler", WriteTimeoutHandler(Constants.MAX_WRITE_TIME_SECONDS))

                            ch.pipeline().addLast("streamer", ChunkedWriteHandler())
                            ch.pipeline().addLast("handler", Http4kChannelHandler(httpHandler))

                            ch.pipeline().addLast("handle_ssl", object : ChannelInboundHandlerAdapter() {
                                override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
                                    if (cause is SSLException || (cause is DecoderException && cause.cause is SSLException)) {
                                        if (LOGGER.isTraceEnabled) {
                                            LOGGER.trace("Ignored invalid SSL connection")
                                        }
                                    } else if (cause is IOException || cause is SocketException) {
                                        if (LOGGER.isInfoEnabled) {
                                            LOGGER.info("User (downloader) abruptly closed the connection")
                                        }
                                        if (LOGGER.isTraceEnabled) {
                                            LOGGER.trace("Exception in pipeline", cause)
                                        }
                                    } else {
                                        ctx.fireExceptionCaught(cause)
                                    }
                                }
                            })
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 1000)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)

            val channel = bootstrap.bind(InetSocketAddress(clientSettings.clientHostname, clientSettings.clientPort)).sync().channel()
            address = channel.localAddress() as InetSocketAddress
            closeFuture = channel.closeFuture()
        }

        override fun stop() = apply {
            masterGroup.shutdownGracefully(1, 15, TimeUnit.SECONDS).sync()
            workerGroup.shutdownGracefully(1, 15, TimeUnit.SECONDS).sync()
            closeFuture.sync()
        }

        override fun port(): Int = if (clientSettings.clientPort > 0) clientSettings.clientPort else address.port
    }
}

fun getX509Certs(certificates: String): Collection<X509Certificate> {
    val targetStream: InputStream = ByteArrayInputStream(certificates.toByteArray())
    @Suppress("unchecked_cast")
    return CertificateFactory.getInstance("X509").generateCertificates(targetStream) as Collection<X509Certificate>
}

fun getPrivateKey(privateKey: String): PrivateKey {
    return loadKey(privateKey)!!
}

package mdnet.base

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFactory
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.ServerChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.HttpUtil
import io.netty.handler.codec.http.HttpVersion
import io.netty.handler.ssl.OptionalSslHandler
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslHandler
import io.netty.handler.stream.ChunkedWriteHandler
import io.netty.handler.traffic.GlobalTrafficShapingHandler
import io.netty.handler.traffic.TrafficCounter
import org.http4k.core.HttpHandler
import org.http4k.server.Http4kChannelHandler
import org.http4k.server.Http4kServer
import org.http4k.server.ServerConfig
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import javax.net.ssl.SSLException

private val LOGGER = LoggerFactory.getLogger("Application")

@ChannelHandler.Sharable
class ConnectionCounter : ChannelInboundHandlerAdapter() {
    private val connections = AtomicInteger()

    override fun channelActive(ctx: ChannelHandlerContext) {
        val sslHandler = ctx.pipeline()[SslHandler::class.java]

        if (sslHandler != null) {
            sslHandler.handshakeFuture().addListener {
                handleConnection(ctx)
            }
        } else {
            handleConnection(ctx)
        }
    }

    private fun handleConnection(ctx: ChannelHandlerContext) {
        if (connections.incrementAndGet() <= Constants.MAX_CONCURRENT_CONNECTIONS) {
            super.channelActive(ctx)
        } else {
            val response = Unpooled.copiedBuffer(Constants.OVERLOADED_MESSAGE, StandardCharsets.UTF_8)
            val res =
                DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.SERVICE_UNAVAILABLE, response)
            res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8")
            HttpUtil.setContentLength(res, response.readableBytes().toLong())

            ctx.writeAndFlush(res)
            ctx.close()
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        super.channelInactive(ctx)
        connections.decrementAndGet()
    }
}

class Netty(private val tls: ServerSettings.TlsCert, private val clientSettings: ClientSettings, private val stats: AtomicReference<Statistics>) : ServerConfig {
    override fun toServer(httpHandler: HttpHandler): Http4kServer = object : Http4kServer {
        private val masterGroup = NioEventLoopGroup()
        private val workerGroup = NioEventLoopGroup()
        private lateinit var closeFuture: ChannelFuture
        private lateinit var address: InetSocketAddress

        private val burstLimiter = object : GlobalTrafficShapingHandler(
            workerGroup, 1024 * clientSettings.maxBurstRateKibPerSecond, 0, 50) {
            override fun doAccounting(counter: TrafficCounter) {
                stats.get().bytesSent.getAndAdd(counter.cumulativeWrittenBytes())
                counter.resetCumulativeTime()
            }
        }
        private val limiter = ConnectionCounter();

        override fun start(): Http4kServer = apply {
            val (mainCert, chainCert) = getX509Certs(tls.certificate);
            val sslContext = SslContextBuilder
                .forServer(getPrivateKey(tls.privateKey), mainCert, chainCert)
                .protocols("TLSv1.3", "TLSv.1.2", "TLSv.1.1", "TLSv.1.0")
                .build()

            val bootstrap = ServerBootstrap()
            bootstrap.group(masterGroup, workerGroup)
                    .channelFactory(ChannelFactory<ServerChannel> { NioServerSocketChannel() })
                    .childHandler(object : ChannelInitializer<SocketChannel>() {
                        public override fun initChannel(ch: SocketChannel) {

                            ch.pipeline().addLast("ssl", OptionalSslHandler(sslContext))
                            ch.pipeline().addLast("limiter", limiter)

                            ch.pipeline().addLast("codec", HttpServerCodec())
                            ch.pipeline().addLast("aggregator", HttpObjectAggregator(65536))
                            ch.pipeline().addLast("burstLimiter", burstLimiter)
                            ch.pipeline().addLast("streamer", ChunkedWriteHandler())
                            ch.pipeline().addLast("handler", Http4kChannelHandler(httpHandler))

                            ch.pipeline().addLast("handle_ssl", object : ChannelInboundHandlerAdapter() {
                                override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
                                    if (cause is SSLException || (cause is DecoderException && cause.cause is SSLException)) {
                                        if (LOGGER.isTraceEnabled) {
                                            LOGGER.trace("Ignored invalid SSL connection")
                                        }
                                    } else if (cause is IOException && cause.message?.contains("peer") == true) {
                                        if (LOGGER.isTraceEnabled) {
                                            LOGGER.trace("User (downloader) closed the connection")
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

            val channel = bootstrap.bind(clientSettings.clientPort).sync().channel()
            address = channel.localAddress() as InetSocketAddress
            closeFuture = channel.closeFuture()
        }

        override fun stop() = apply {
            masterGroup.shutdownGracefully(5, 15, TimeUnit.SECONDS).sync()
            workerGroup.shutdownGracefully(5, 15, TimeUnit.SECONDS).sync()
            closeFuture.sync()
        }

        override fun port(): Int = if (clientSettings.clientPort > 0) clientSettings.clientPort else address.port
    }
}

fun getX509Certs(certificates: String): Pair<X509Certificate, X509Certificate> {
    val targetStream: InputStream = ByteArrayInputStream(certificates.toByteArray())
    return (CertificateFactory.getInstance("X509").generateCertificate(targetStream) as X509Certificate) to (CertificateFactory.getInstance("X509").generateCertificate(targetStream) as X509Certificate)
}

fun getPrivateKey(privateKey: String): PrivateKey {
    return loadKey(privateKey)!!
}
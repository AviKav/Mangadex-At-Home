package mdnet.base.netty

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFactory
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.ServerChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.HttpServerKeepAliveHandler
import io.netty.handler.stream.ChunkedWriteHandler
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import org.http4k.core.HttpHandler
import org.http4k.server.Http4kChannelHandler
import org.http4k.server.Http4kServer
import org.http4k.server.ServerConfig
import org.slf4j.LoggerFactory

private val LOGGER = LoggerFactory.getLogger(WebUiNetty::class.java)

class WebUiNetty(private val hostname: String, private val port: Int) : ServerConfig {
    override fun toServer(httpHandler: HttpHandler): Http4kServer = object : Http4kServer {
        private val masterGroup = NioEventLoopGroup()
        private val workerGroup = NioEventLoopGroup()
        private lateinit var closeFuture: ChannelFuture
        private lateinit var address: InetSocketAddress

        override fun start(): Http4kServer = apply {
            val bootstrap = ServerBootstrap()
            bootstrap.group(masterGroup, workerGroup)
                    .channelFactory(ChannelFactory<ServerChannel> { NioServerSocketChannel() })
                    .childHandler(object : ChannelInitializer<SocketChannel>() {
                        public override fun initChannel(ch: SocketChannel) {
                            ch.pipeline().addLast("codec", HttpServerCodec())
                            ch.pipeline().addLast("keepAlive", HttpServerKeepAliveHandler())
                            ch.pipeline().addLast("aggregator", HttpObjectAggregator(Int.MAX_VALUE))
                            ch.pipeline().addLast("streamer", ChunkedWriteHandler())
                            ch.pipeline().addLast("handler", Http4kChannelHandler(httpHandler))
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 1000)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)

            val channel = bootstrap.bind(InetSocketAddress(hostname, port)).sync().channel()
            address = channel.localAddress() as InetSocketAddress
            closeFuture = channel.closeFuture()
        }

        override fun stop() = apply {
            masterGroup.shutdownGracefully(5, 15, TimeUnit.SECONDS).sync()
            workerGroup.shutdownGracefully(5, 15, TimeUnit.SECONDS).sync()
            closeFuture.sync()
        }

        override fun port(): Int = address.port
    }
}

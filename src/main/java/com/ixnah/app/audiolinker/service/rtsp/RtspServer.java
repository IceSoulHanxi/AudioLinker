package com.ixnah.app.audiolinker.service.rtsp;

import com.ixnah.app.audiolinker.service.capture.CaptureService;
import com.ixnah.app.audiolinker.service.rtp.RtpEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.rtsp.RtspDecoder;
import io.netty.handler.codec.rtsp.RtspEncoder;

import java.util.concurrent.atomic.AtomicReference;

public class RtspServer implements Runnable {

    private final AtomicReference<CaptureService> captureServiceRef = new AtomicReference<>();
    private final int port;

    public RtspServer(int port) {
        this.port = port;
    }

    public void setCaptureService(CaptureService captureService) {
        captureServiceRef.set(captureService);
    }

    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        RtspServerHandler rtspServerHandler = new RtspServerHandler(captureServiceRef::get);

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            // 添加自定义的RTSP处理器
                            ch.pipeline().addLast(new RtspDecoder());
                            ch.pipeline().addLast(new RtspEncoder());
                            ch.pipeline().addLast(new RtpEncoder.TCP());
                            ch.pipeline().addLast(rtspServerHandler);
                        }
                    });

            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

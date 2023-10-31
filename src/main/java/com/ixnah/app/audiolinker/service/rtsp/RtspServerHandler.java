package com.ixnah.app.audiolinker.service.rtsp;

import com.ixnah.app.audiolinker.Metadata;
import com.ixnah.app.audiolinker.service.rtp.RtpPacket;
import com.ixnah.app.audiolinker.service.capture.CaptureService;
import com.ixnah.app.audiolinker.util.NettyUtil;
import com.ixnah.app.audiolinker.util.SdpUtil;
import com.ixnah.app.audiolinker.util.UnsafeUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.rtsp.RtspMethods;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ixnah.app.audiolinker.service.rtp.Constant.RTP_SEQUENCE;
import static com.ixnah.app.audiolinker.service.rtp.Constant.RTP_TIMESTAMP;
import static io.netty.handler.codec.rtsp.RtspHeaderNames.*;
import static io.netty.handler.codec.rtsp.RtspResponseStatuses.*;

public class RtspServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(RtspServerHandler.class);
    private static final Map<String, HttpMethod> RTSP_METHOD_MAP =
            Objects.requireNonNullElse(UnsafeUtil.getStatic(RtspMethods.class, "methodMap"), Collections.emptyMap());
    private static final AttributeKey<Integer> LISTENER_ID = AttributeKey.newInstance("LISTENER_ID");
    private static final int PAYLOAD_TYPE_AAC = 97;
    private final Supplier<CaptureService> serviceSupplier;

    public RtspServerHandler(Supplier<CaptureService> serviceSupplier) {
        this.serviceSupplier = serviceSupplier;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        LOG.info("new connection: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest request) {
            handleRequest(ctx, request);
        } else if (msg instanceof HttpContent content) {
            handleContent(ctx, content);
        } else {
            LOG.warn("no handle message: " + Objects.toIdentityString(msg));
        }
    }

    private void handleRequest(ChannelHandlerContext ctx, HttpRequest request) {
        HttpMethod method = request.method();
        if (!RTSP_METHOD_MAP.containsValue(method)) {
            LOG.warn("unsupported request method: " + method);
            return;
        }
        LOG.debug("handle request: " + request);

        if (RtspMethods.OPTIONS.equals(method)) {
            handleRequestOptions(ctx, request);
            return;
        }

        if (RtspMethods.DESCRIBE.equals(method)) {
            handleRequestDescribe(ctx, request);
            return;
        }

        if (RtspMethods.SETUP.equals(method)) {
            handleRequestSetup(ctx, request);
            return;
        }

        if (RtspMethods.PLAY.equals(method)) {
            handleRequestPlay(ctx, request);
            return;
        }

        if (RtspMethods.TEARDOWN.equals(method)) {
            handleRequestTeardown(ctx, request);
            return;
        }

//        String uri = request.uri();
//        QueryStringDecoder decoder = new QueryStringDecoder(uri, StandardCharsets.UTF_8);
//        String path = decoder.path();
//        Map<String, List<String>> parameters = decoder.parameters();
        DefaultHttpResponse response = new DefaultHttpResponse(request.protocolVersion(), METHOD_NOT_ALLOWED);
        ctx.channel().writeAndFlush(response);
    }

    private void handleRequestOptions(ChannelHandlerContext ctx, HttpRequest request) {
        var reqHeaders = request.headers();
        var headers = new DefaultHttpHeaders();
        headers.add(SERVER, Metadata.AppIdVersion);
        headers.add(CSEQ, reqHeaders.get(CSEQ, "1"));
        String supportedMethod = Stream.of(RtspMethods.OPTIONS, RtspMethods.DESCRIBE, RtspMethods.SETUP, RtspMethods.PLAY, RtspMethods.TEARDOWN)
                .map(HttpMethod::toString).collect(Collectors.joining(", "));
        headers.add(PUBLIC, supportedMethod);
        var response = new DefaultHttpResponse(request.protocolVersion(), OK, headers);
        ctx.channel().writeAndFlush(response);
    }

    private void handleRequestDescribe(ChannelHandlerContext ctx, HttpRequest request) {
        var uri = URI.create(request.uri());
        var host = uri.getHost();
        int port = uri.getPort();
        var baseUri = "rtsp://" + host + (port == -1 ? "" : ":" + port) + "/";
        var currentTime = "" + System.currentTimeMillis();

        var reqHeaders = request.headers();
        var headers = new DefaultHttpHeaders();
        headers.add(SERVER, Metadata.AppIdVersion);
        headers.add(CSEQ, reqHeaders.get(CSEQ, "2"));

        CaptureService service = serviceSupplier.get();
        if (service == null || service.stopped()) {
            ctx.channel().writeAndFlush(new DefaultHttpResponse(request.protocolVersion(), SERVICE_UNAVAILABLE, headers));
            return;
        }

        var content = Unpooled.buffer();
        SdpUtil.addOption(content, "v", "0");
        // o=<username> <session id> <version> <network type> <address type> <address>
        SdpUtil.addOption(content, "o", "-", currentTime, currentTime, "IN", "IP4", host);
        SdpUtil.addOption(content, "t", "0 0");
        // m=<media> <port> <transport type> <fmt list>
        SdpUtil.addOption(content, "m", "audio 0 RTP/AVP/TCP " + PAYLOAD_TYPE_AAC); // UDP: RTP/AVP
        // 获取当前音频设备采样率、通道数
        int sampleRate = SdpUtil.convertSimpleRate(service.getSimpleRate());
        int channel = service.getChannelCount();
        int aacProfile = 1; // AAC-LC
        int aacConfig = (aacProfile + 1) << 5; // aac的profile，通常情况是1或2
        aacConfig |= sampleRate << 4; // aac的采样频率的索引
        aacConfig |= channel << 3;
        //转成16进制
        aacConfig = ((aacConfig >> 12) & 0xF) * 1000 + ((aacConfig >> 8) & 0xF) * 100 + ((aacConfig >> 4) & 0xF) * 10 + (aacConfig & 0xF);
        // 默认使用AAC TODO: 支持例如Opus等其他格式
        SdpUtil.addOption(content, "a", "rtpmap:" + PAYLOAD_TYPE_AAC + " MPEG4-GENERIC/" + sampleRate + "/" + channel);
        SdpUtil.addOption(content, "a", "fmtp:" + PAYLOAD_TYPE_AAC + " profile-level-id=1;mode=AAC-hbr;sizelength=13;indexlength=3;indexdeltalength=3;config=" + aacConfig);
        SdpUtil.addOption(content, "a", "control:streamid=1");

        headers.add(CONTENT_BASE, baseUri);
        headers.add(CONTENT_TYPE, "application/sdp");
        headers.add(CONTENT_LENGTH, content.writerIndex());

        var protocol = request.protocolVersion();
        var response = new DefaultFullHttpResponse(protocol, OK, content, headers, EmptyHttpHeaders.INSTANCE);
        ctx.channel().writeAndFlush(response);
    }

    private void handleRequestSetup(ChannelHandlerContext ctx, HttpRequest request) {
        var reqHeaders = request.headers();
        var headers = new DefaultHttpHeaders();
        headers.add(SERVER, Metadata.AppIdVersion);
        headers.add(CSEQ, reqHeaders.get(CSEQ, "3"));

        CaptureService service = serviceSupplier.get();
        if (service == null || service.stopped()) {
            ctx.channel().writeAndFlush(new DefaultHttpResponse(request.protocolVersion(), SERVICE_UNAVAILABLE, headers));
            return;
        }

        // TODO: 设置AAC encoder

        if (!service.capturing()) {
            service.start();
        }

//        headers.add(TRANSPORT, "RTP/AVP;unicast;client_port=%d-%d;server_port=%d-%d"); // UDP
        headers.add(TRANSPORT, "RTP/AVP/TCP;unicast;interleaved=0-1");

        var response = new DefaultHttpResponse(request.protocolVersion(), OK, headers);
        ctx.channel().writeAndFlush(response);
    }

    private void handleRequestPlay(ChannelHandlerContext ctx, HttpRequest request) {
        var reqHeaders = request.headers();
        var headers = new DefaultHttpHeaders();
        headers.add(SERVER, Metadata.AppIdVersion);
        headers.add(CSEQ, reqHeaders.get(CSEQ, "4"));

        CaptureService service = serviceSupplier.get();
        if (service == null || service.stopped()) {
            ctx.channel().writeAndFlush(new DefaultHttpResponse(request.protocolVersion(), SERVICE_UNAVAILABLE, headers));
            return;
        }

        var response = new DefaultHttpResponse(request.protocolVersion(), OK, headers);
        ctx.channel().writeAndFlush(response);
        int listenerId = service.addListener(buffer -> {
            CompletableFuture<?> result = CompletableFuture.completedFuture(true);
            if (ctx.channel().hasAttr(LISTENER_ID)) {
                Attribute<AtomicInteger> rtpSequence = ctx.channel().attr(RTP_SEQUENCE);
                AtomicInteger sequence;
                if (ctx.channel().hasAttr(RTP_SEQUENCE)) {
                    sequence = rtpSequence.get();
                } else {
                    sequence = new AtomicInteger(1);
                    rtpSequence.setIfAbsent(sequence);
                }
                int seq = sequence.getAndIncrement();

                Attribute<Long> rtpTimestamp = ctx.channel().attr(RTP_TIMESTAMP);
                long timestamp;
                if (ctx.channel().hasAttr(RTP_TIMESTAMP)) {
                    timestamp = rtpTimestamp.get();
                } else {
                    timestamp = System.currentTimeMillis();
                    rtpTimestamp.setIfAbsent(timestamp);
                }
                int time = (int) (System.currentTimeMillis() - timestamp);

                int ssrc = ctx.channel().attr(LISTENER_ID).get();

                ChannelFuture future = ctx.channel().writeAndFlush(new RtpPacket(PAYLOAD_TYPE_AAC, seq, time, ssrc, buffer));
                result = NettyUtil.toCompletable(future);
            } else {
                LOG.warn("Channel {} listener id not found! Disconnect.", ctx.channel());
                ctx.channel().close();
            }
            return result;
        });
        ctx.channel().attr(LISTENER_ID).set(listenerId);
    }

    private void handleRequestTeardown(ChannelHandlerContext ctx, HttpRequest request) {
        var reqHeaders = request.headers();
        var headers = new DefaultHttpHeaders();
        headers.add(SERVER, Metadata.AppIdVersion);
        headers.add(CSEQ, reqHeaders.get(CSEQ, "5"));

        CaptureService service = serviceSupplier.get();
        if (service == null || service.stopped()) {
            ctx.channel().writeAndFlush(new DefaultHttpResponse(request.protocolVersion(), SERVICE_UNAVAILABLE, headers));
            return;
        }

        if (ctx.channel().hasAttr(LISTENER_ID)) {
            service.removeListener(ctx.channel().attr(LISTENER_ID).getAndSet(null));
        }
        var response = new DefaultHttpResponse(request.protocolVersion(), OK, headers);
        ctx.channel().writeAndFlush(response);
    }

    private void handleContent(ChannelHandlerContext ctx, HttpContent content) {

    }
}

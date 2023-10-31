package com.ixnah.app.audiolinker.service.rtp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RtpEncoder extends MessageToByteEncoder<RtpPacket> {
    @Override
    protected void encode(ChannelHandlerContext ctx, RtpPacket msg, ByteBuf out) {
        new RtpHeader(
                msg.version, 0, msg.extension ? 1 : 0, msg.csrcLength,
                0, msg.payloadType, msg.seq,
                msg.timestamp,
                msg.ssrc
        ).write(out);
        out.writeBytes(msg.payload);
    }

    public static class TCP extends RtpEncoder {
        @Override
        protected void encode(ChannelHandlerContext ctx, RtpPacket msg, ByteBuf out) {
            Integer channelId = ctx.channel().attr(Constant.RTP_CHANNEL).get();
            channelId = channelId != null ? channelId : 0;
            int rtpSize = msg.payload.capacity() + 12;
            out.writeByte(0x24); // $
            out.writeByte(channelId);
            out.writeByte(((rtpSize) & 0xFF00) >> 8);
            out.writeByte((rtpSize) & 0xFF);
            super.encode(ctx, msg, out);
        }
    }
}

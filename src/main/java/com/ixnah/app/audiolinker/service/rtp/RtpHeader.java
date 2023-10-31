package com.ixnah.app.audiolinker.service.rtp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class RtpHeader {
    private final byte version;
    private final byte padding;
    private final byte extension;
    private final byte csrcLen;
    private final byte marker;
    private final byte payloadType;
    private final short seq;
    private final int timestamp;
    private final int ssrc;

    public RtpHeader(int version, int padding, int extension, int csrcLen, int marker, int payloadType, int seq, int timestamp, int ssrc) {
        this.version = (byte) version;
        this.padding = (byte) padding;
        this.extension = (byte) extension;
        this.csrcLen = (byte) csrcLen;
        this.marker = (byte) marker;
        this.payloadType = (byte) payloadType;
        this.seq = (short) seq;
        this.timestamp = timestamp;
        this.ssrc = ssrc;
    }

    public ByteBuf toByteBuf() {
        ByteBuf byteBuf = Unpooled.buffer(12);
        byteBuf.writeByte((version << 6) | (padding << 5) | (extension << 4) | csrcLen);
        byteBuf.writeByte((marker << 7) | payloadType);
        byteBuf.writeShort(seq);
        byteBuf.writeInt(timestamp);
        byteBuf.writeInt(ssrc);
        return byteBuf;
    }

    public ByteBuf write(ByteBuf byteBuf) {
        byteBuf.ensureWritable(12);
        byteBuf.writeByte((version << 6) | (padding << 5) | (extension << 4) | csrcLen);
        byteBuf.writeByte((marker << 7) | payloadType);
        byteBuf.writeShort(seq);
        byteBuf.writeInt(timestamp);
        byteBuf.writeInt(ssrc);
        return byteBuf;
    }
}
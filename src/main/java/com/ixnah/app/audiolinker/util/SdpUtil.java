package com.ixnah.app.audiolinker.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;

public class SdpUtil {

    private SdpUtil() {
        throw new UnsupportedOperationException();
    }

    public static ByteBuf addOption(ByteBuf buf, String key, String... values) {
        ByteBufUtil.writeUtf8(buf, key);
        buf.writeChar('=');
        for (String value : values) {
            ByteBufUtil.writeUtf8(buf, value);
            buf.markWriterIndex();
            buf.writeChar(' ');
        }
        buf.resetWriterIndex();
        return buf.writeChar('\r').writeChar('\n');
    }

    public static int convertSimpleRate(int simpleRate) {
        return switch (simpleRate) {
            case 96000 -> 0;
            case 88200 -> 1;
            case 64000 -> 2;
            case 48000 -> 3;
            case 44100 -> 4;
            case 32000 -> 5;
            case 24000 -> 6;
            case 22050 -> 7;
            case 16000 -> 8;
            case 12000 -> 9;
            case 11025 -> 10;
            case 8000 -> 11;
            case 7350 -> 12;
            default -> 15;
        };
    }
}

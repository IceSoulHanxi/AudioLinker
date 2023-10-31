package com.ixnah.app.audiolinker.service.rtp;

import io.netty.util.AttributeKey;

import java.util.concurrent.atomic.AtomicInteger;

public class Constant {

    private Constant() {
        throw new UnsupportedOperationException();
    }

    public static final AttributeKey<Integer> RTP_CHANNEL = AttributeKey.newInstance("RTP_CHANNEL");
    public static final AttributeKey<AtomicInteger> RTP_SEQUENCE = AttributeKey.newInstance("RTP_SEQUENCE");
    public static final AttributeKey<Long> RTP_TIMESTAMP = AttributeKey.newInstance("RTP_TIMESTAMP");
}

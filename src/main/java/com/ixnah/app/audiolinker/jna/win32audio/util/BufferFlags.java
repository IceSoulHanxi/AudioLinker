package com.ixnah.app.audiolinker.jna.win32audio.util;

import com.sun.jna.platform.win32.COM.util.IComEnum;

public enum BufferFlags implements IComEnum {
    DataDiscontinuity(0x1),
    Silent(0x2),
    TimestampError(0x4),
    ;

    BufferFlags(long value) {
        this.value = value;
    }

    private final long value;

    @Override
    public long getValue() {
        return this.value;
    }
}

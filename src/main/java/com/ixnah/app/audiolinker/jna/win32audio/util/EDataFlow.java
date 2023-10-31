
package com.ixnah.app.audiolinker.jna.win32audio.util;

import com.sun.jna.platform.win32.COM.util.IComEnum;

public enum EDataFlow implements IComEnum {

    /**
     * (0)
     */
    eRender(0),

    /**
     * (1)
     */
    eCapture(1),

    /**
     * (2)
     */
    eAll(2),

    /**
     * (3)
     */
    EDataFlow_enum_count(3),
    ;

    EDataFlow(long value) {
        this.value = value;
    }

    private final long value;

    @Override
    public long getValue() {
        return this.value;
    }
}
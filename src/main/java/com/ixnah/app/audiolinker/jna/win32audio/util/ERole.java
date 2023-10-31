
package com.ixnah.app.audiolinker.jna.win32audio.util;

import com.sun.jna.platform.win32.COM.util.IComEnum;

public enum ERole implements IComEnum {

    /**
     * (0)
     */
    eConsole(0),

    /**
     * (1)
     */
    eMultimedia(1),

    /**
     * (2)
     */
    eCommunications(2),

    /**
     * (3)
     */
    ERole_enum_count(3),
    ;

    ERole(long value) {
        this.value = value;
    }

    private final long value;

    @Override
    public long getValue() {
        return this.value;
    }
}
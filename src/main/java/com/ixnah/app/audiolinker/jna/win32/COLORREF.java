package com.ixnah.app.audiolinker.jna.win32;

import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef;

public class COLORREF extends WinDef.DWORD {
    public COLORREF() {
        super();
    }

    public COLORREF(long value) {
        super(value);
    }

    public int getRGB() {
        return intValue();
    }

    public int getRed() {
        return (getRGB() >> 16) & 0xFF;
    }

    public int getGreen() {
        return (getRGB() >> 8) & 0xFF;
    }

    public int getBlue() {
        return getRGB() & 0xFF;
    }

    public static class ByReference extends COLORREF implements Structure.ByReference {
        public ByReference() {
        }

        public ByReference(long value) {
            super(value);
        }
    }

    public static class ByValue extends COLORREF implements Structure.ByValue {
        public ByValue() {
        }

        public ByValue(long value) {
            super(value);
        }
    }
}
package com.ixnah.app.audiolinker.jna.win32audio.structure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef;

@Structure.FieldOrder({"wFormatTag", "nChannels", "nSamplesPerSec", "nAvgBytesPerSec", "nBlockAlign", "wBitsPerSample", "cbSize"})
public class WAVEFORMATEX extends Structure {
    public WinDef.USHORT wFormatTag;
    public short nChannels;
    public int nSamplesPerSec;
    public int nAvgBytesPerSec;
    public short nBlockAlign;
    public short wBitsPerSample;
    public short cbSize;

    public static class ByReference extends WAVEFORMATEX implements Structure.ByReference {
        public ByReference() {
        }

        public ByReference(Pointer memory) {
            super(memory);
        }
    }

    public WAVEFORMATEX() {
        super();
    }

    public WAVEFORMATEX(Pointer memory) {
        super(memory);
        read();
    }
}
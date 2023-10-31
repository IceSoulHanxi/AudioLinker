package com.ixnah.app.audiolinker.jna.fdkaac.structure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Maps to AACENC_BufDesc struct.
 *
 * @see <a href="https://github.com/mstorsjo/fdk-aac/blob/v0.1.6/libAACenc/include/aacenc_lib.h">fdk-aac/libAACenc/include/aacenc_lib.h</a>
 */
@Structure.FieldOrder({"numBufs", "bufs", "bufferIdentifiers", "bufSizes", "bufElSizes"})
public final class AACEncBufDesc extends Structure {

    public int numBufs;
    public PointerByReference bufs;
    public IntByReference bufferIdentifiers;
    public IntByReference bufSizes;
    public IntByReference bufElSizes;

    public AACEncBufDesc() {
        super();
    }

    public AACEncBufDesc(Pointer memory) {
        super();
        read();
    }
}

package com.ixnah.app.audiolinker.jna.fdkaac.structure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * Maps to AACENC_InArgs struct.
 *
 * @see <a href="https://github.com/mstorsjo/fdk-aac/blob/v0.1.6/libAACenc/include/aacenc_lib.h">fdk-aac/libAACenc/include/aacenc_lib.h</a>
 */
@Structure.FieldOrder({"numInSamples", "numAncBytes"})
public class AACEncInArgs extends Structure {

    public int numInSamples;
    public int numAncBytes;

    public AACEncInArgs() {
        super();
    }

    public AACEncInArgs(Pointer memory) {
        super(memory);
        read();
    }
}

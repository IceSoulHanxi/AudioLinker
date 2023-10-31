package com.ixnah.app.audiolinker.jna.fdkaac.structure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * Maps to AACENC_OutArgs struct.
 *
 * @see <a href="https://github.com/mstorsjo/fdk-aac/blob/v0.1.6/libAACenc/include/aacenc_lib.h">fdk-aac/libAACenc/include/aacenc_lib.h</a>
 */
@Structure.FieldOrder({"numOutBytes", "numInSamples", "numAncBytes"})
public class AACEncOutArgs extends Structure {

    public int numOutBytes;
    public int numInSamples;
    public int numAncBytes;

    public AACEncOutArgs() {
        super();
    }

    public AACEncOutArgs(Pointer memory) {
        super();
        read();
    }
}

package com.ixnah.app.audiolinker.jna.fdkaac.structure;

import com.sun.jna.Structure;

/**
 * Maps to AACENC_InfoStruct struct.
 * <p>
 * in @see <a href="https://github.com/mstorsjo/fdk-aac/blob/v0.1.6/libAACenc/include/aacenc_lib.h">fdk-aac/libAACenc/include/aacenc_lib.h</a>
 */
@Structure.FieldOrder({"maxOutBufBytes", "maxAncBytes", "inBufFillLevel", "inputChannels", "frameLength", "encoderDelay", "confBuf", "confSize"})
public class AACEncInfo extends Structure {

    private static final int CONF_BUF_SIZE = 64;

    public int maxOutBufBytes;
    public int maxAncBytes;
    public int inBufFillLevel;
    public int inputChannels;
    public int frameLength;
    public int encoderDelay;
    public byte[] confBuf = new byte[CONF_BUF_SIZE];
    public int confSize;

}

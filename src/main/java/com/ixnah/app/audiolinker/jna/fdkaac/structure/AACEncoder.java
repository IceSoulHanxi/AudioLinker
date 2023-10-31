package com.ixnah.app.audiolinker.jna.fdkaac.structure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.ShortByReference;

/**
 * Maps to AACENCODER struct.
 *
 * @see <a href="https://github.com/mstorsjo/fdk-aac/blob/v0.1.6/libAACenc/src/aacenc_lib.cpp">fdk-aac/libAACenc/src/aacenc_lib.cpp</a>
 */
@Structure.FieldOrder({"extParam", "coderConfig", "aacConfig", "hAacEnc", "hEnvEnc", "hMetadataEnc", "metaDataAllowed",
        "hTpEnc", "outBuffer", "outBufferInBytes", "inputBuffer", "inputBufferOffset", "nSamplesToRead", "nSamplesRead",
        "nZerosAppended", "nDelay", "extPayload", "extPayloadData", "extPayloadSize", "InitFlags", "nMaxAacElements",
        "nMaxAacChannels", "nMaxSbrElements", "nMaxSbrChannels", "nMaxSubFrames", "encoder_modis", "CAPF_tpEnc"})
public class AACEncoder extends Structure {

    private static final int MAX_TOTAL_EXT_PAYLOADS = 12;
    private static final int MAX_PAYLOAD_SIZE = 256;

    public UserParam extParam;
    public CoderConfig coderConfig;
    public AACEncConfig aacConfig;
    public Pointer hAacEnc;
    public Pointer hEnvEnc;
    public Pointer hMetadataEnc;
    public int metaDataAllowed;
    public Pointer hTpEnc;
    public ByteByReference outBuffer;
    public int outBufferInBytes;
    public ShortByReference inputBuffer;
    public int inputBufferOffset;
    public int nSamplesToRead;
    public int nSamplesRead;
    public int nZerosAppended;
    public int nDelay;
    /*
        Arrays - 1D, 2D or even 3D, are initialized as 1D to accommodate for size allocation.
        This is due to the fact JNA doesn't support mapping multi-dimensional array inside structs with simple
        preservation of size/memory allocation for primitives. As we won't be using these values, I'm
        perfectly fine with it
     */
    public AACEncExtPayload[] extPayload = new AACEncExtPayload[MAX_TOTAL_EXT_PAYLOADS];
    public byte[] extPayloadData = new byte[8 * MAX_PAYLOAD_SIZE];
    public int[] extPayloadSize = new int[8];
    public long InitFlags;
    public int nMaxAacElements;
    public int nMaxAacChannels;
    public int nMaxSbrElements;
    public int nMaxSbrChannels;
    public int nMaxSubFrames;
    public int encoder_modis;
    public int CAPF_tpEnc;

    public static class ByReference extends AACEncoder implements Structure.ByReference {
        public ByReference() {
        }

        public ByReference(Pointer memory) {
            super(memory);
        }
    }

    public AACEncoder() {
        super();
    }

    private AACEncoder(Pointer memory) {
        super(memory);
        setAlignType(Structure.ALIGN_NONE); // Make sure field size alignments are as expected
        read(); // Read once after initialize from provided pointer
    }
}

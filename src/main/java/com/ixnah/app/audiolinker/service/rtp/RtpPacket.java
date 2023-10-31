package com.ixnah.app.audiolinker.service.rtp;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class RtpPacket implements Serializable {

    /*
     *    0                   1                   2                   3
     *    7 6 5 4 3 2 1 0|7 6 5 4 3 2 1 0|7 6 5 4 3 2 1 0|7 6 5 4 3 2 1 0
     *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *   |V=2|P|X|  CC   |M|     PT      |       sequence number         |
     *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *   |                           timestamp                           |
     *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *   |           synchronization source (SSRC) identifier            |
     *   +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+
     *   |            contributing source (CSRC) identifiers             |
     *   :                             ....                              :
     *   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     *
     */

    final int version;
    // padding
    final boolean extension;
    final int csrcLength;
    // mark
    final int payloadType;
    final int seq;
    final int timestamp;
    final int ssrc;
    final int csrc;
    final ByteBuffer payload;

    public RtpPacket(int payloadType, int seq, int timestamp, int ssrc, ByteBuffer payload) {
        this(false, 0, payloadType, seq, timestamp, ssrc, 0, payload);
    }

    public RtpPacket(boolean extension, int csrcLength, int payloadType, int seq, int timestamp, int ssrc, int csrc, ByteBuffer payload) {
        this(2, extension, csrcLength, payloadType, seq, timestamp, ssrc, csrc, payload);
    }

    public RtpPacket(int version, boolean extension, int csrcLength, int payloadType, int seq, int timestamp, int ssrc, int csrc, ByteBuffer payload) {
        this.version = version;
        this.extension = extension;
        this.csrcLength = csrcLength;
        this.payloadType = payloadType;
        this.seq = seq;
        this.timestamp = timestamp;
        this.ssrc = ssrc;
        this.csrc = csrc;
        this.payload = payload;
    }
}

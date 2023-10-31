package com.ixnah.app.audiolinker.jna.fdkaac.util;

import com.ixnah.app.audiolinker.jna.util.IValueEnum;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Maps to AACENC_ERROR enum.
 *
 * @see <a href="https://github.com/mstorsjo/fdk-aac/blob/v0.1.6/libAACenc/include/aacenc_lib.h">fdk-aac/libAACenc/include/aacenc_lib.h</a>
 */
public enum AACEncError implements IValueEnum {

    Unknown(-0x0001),
    Ok(0x0000),
    InvalidHandle(0x0020),
    MemoryError(0x0021),
    UnsupportedParameter(0x0022),
    InvalidConfig(0x0023),
    InitError(0x0040),
    InitAACError(0x0041),
    InitSBRError(0x0042),
    InitTPError(0x0043),
    InitMetaError(0x0044),
    EncodeError(0x0060),
    EncodeEOF(0x0080);

    private static final Map<Integer, AACEncError> BY_CODE = Stream.of(values())
            .collect(Collectors.toMap(AACEncError::getValue, err -> err));

    AACEncError(int value) {
        this.value = value;
    }

    /**
     * Match a {@link AACEncError} from a given numeric error code.
     *
     * @param value numeric library error code
     * @return the matched library error descriptor
     */
    public static AACEncError valueOf(final int value) {
        return BY_CODE.getOrDefault(value, Unknown);
    }

    private final int value;

    @Override
    public int getValue() {
        return value;
    }

    public void throwWhenNotOk() {
        if (!this.equals(Ok)) {
            throw new FdkAACException(this);
        }
    }
}

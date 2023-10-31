package com.ixnah.app.audiolinker.jna.fdkaac;

import com.ixnah.app.audiolinker.jna.fdkaac.structure.*;
import com.ixnah.app.audiolinker.jna.fdkaac.util.AACEncError;
import com.ixnah.app.audiolinker.jna.fdkaac.util.AACEncParam;
import com.ixnah.app.audiolinker.jna.util.AppTypeMapper;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;

import java.util.Collections;

public interface FdkAac extends StdCallLibrary {
    FdkAac INSTANCE = Native.load("fdk-aac", FdkAac.class, Collections.singletonMap(Library.OPTION_TYPE_MAPPER, new AppTypeMapper()));

    AACEncError aacEncOpen(PointerByReference phAacEncoder, int encModules, int maxChannels);

    AACEncError aacEncClose(PointerByReference phAacEncoder);

    AACEncError aacEncEncode(
            Pointer hAacEncoder,
            AACEncBufDesc inBufDesc,
            AACEncBufDesc outBufDesc,
            AACEncInArgs inargs,
            AACEncOutArgs outargs);

    AACEncError aacEncInfo(Pointer hAacEncoder, AACEncInfo pInfo);

    AACEncError aacEncoder_SetParam(Pointer encoder, AACEncParam param, int value);

    int aacEncoder_GetParam(Pointer encoder, AACEncParam param);
}

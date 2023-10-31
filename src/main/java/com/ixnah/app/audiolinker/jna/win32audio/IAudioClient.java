package com.ixnah.app.audiolinker.jna.win32audio;

import com.ixnah.app.audiolinker.jna.win32audio.structure.WAVEFORMATEX;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.COM.util.IUnknown;
import com.sun.jna.platform.win32.COM.util.annotation.ComInterface;
import com.sun.jna.platform.win32.COM.util.annotation.ComMethod;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.ptr.PointerByReference;

@ComInterface(iid = "{1CB9AD4C-DBFA-4c32-B178-C2F568A703B2}")
public interface IAudioClient extends IUnknown {

    @ComMethod(dispId = 1, name = "Initialize")
    HRESULT initialize(
            int ShareMode,
            int StreamFlags,
            long hnsBufferDuration,
            long hnsPeriodicity,
            WAVEFORMATEX.ByReference pFormat,
            Guid.GUID AudioSessionGuid
    );

    @ComMethod(dispId = 2, name = "GetBufferSize")
    HRESULT getBufferSize(WinDef.UINTByReference pNumBufferFrames);

    @ComMethod(dispId = 3, name = "GetStreamLatency")
    HRESULT getStreamLatency(WinDef.LONGLONGByReference phnsLatency);

    @ComMethod(dispId = 4, name = "GetCurrentPadding")
    HRESULT getCurrentPadding(WinDef.UINTByReference pNumPaddingFrames);

    @ComMethod(dispId = 5, name = "IsFormatSupported")
    HRESULT isFormatSupported(
            int ShareMode,
            WAVEFORMATEX.ByReference pFormat,
            PointerByReference ppClosestMatch
    );

    @ComMethod(dispId = 6, name = "GetMixFormat")
    HRESULT getMixFormat(PointerByReference ppDeviceFormat);

    @ComMethod(dispId = 7, name = "GetDevicePeriod")
    HRESULT getDevicePeriod(
            WinDef.LONGLONGByReference phnsDefaultDevicePeriod,
            WinDef.LONGLONGByReference phnsMinimumDevicePeriod
    );

    @ComMethod(dispId = 8, name = "Start")
    HRESULT start();

    @ComMethod(dispId = 9, name = "Stop")
    HRESULT stop();

    @ComMethod(dispId = 10, name = "Reset")
    HRESULT reset();

    @ComMethod(dispId = 11, name = "SetEventHandle")
    HRESULT setEventHandle(Pointer eventHandle);

    @ComMethod(dispId = 12, name = "GetService")
    HRESULT getService(
            Guid.GUID riid,
            PointerByReference ppv
    );
}
package com.ixnah.app.audiolinker.jna.win32audio;

import com.sun.jna.platform.win32.COM.util.IUnknown;
import com.sun.jna.platform.win32.COM.util.annotation.ComInterface;
import com.sun.jna.platform.win32.COM.util.annotation.ComMethod;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;

@ComInterface(iid = "{C8ADBD64-E71E-48a0-A4DE-185C395CD317}")
public interface IAudioCaptureClient extends IUnknown {

    @ComMethod(dispId = 1, name = "GetBuffer")
    HRESULT getBuffer(
            PointerByReference ppData,
            IntByReference pNumFramesToRead,
            IntByReference pdwFlags,
            LongByReference pu64DevicePosition,
            LongByReference pu64QPCPosition
    );

    @ComMethod(dispId = 2, name = "ReleaseBuffer")
    HRESULT releaseBuffer(int NumFramesRead);

    @ComMethod(dispId = 3, name = "GetNextPacketSize")
    HRESULT getNextPacketSize(IntByReference pNumFramesInNextPacket);
}
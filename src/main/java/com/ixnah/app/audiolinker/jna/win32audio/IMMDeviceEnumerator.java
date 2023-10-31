package com.ixnah.app.audiolinker.jna.win32audio;

import com.ixnah.app.audiolinker.jna.win32audio.util.EDataFlow;
import com.ixnah.app.audiolinker.jna.win32audio.util.ERole;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.COM.util.IUnknown;
import com.sun.jna.platform.win32.COM.util.annotation.ComInterface;
import com.sun.jna.platform.win32.COM.util.annotation.ComMethod;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.ptr.PointerByReference;

@ComInterface(iid = "{A95664D2-9614-4F35-A746-DE8DB63617E6}")
public interface IMMDeviceEnumerator extends IUnknown {

    @ComMethod(dispId = 1, name = "EnumAudioEndpoints")
    HRESULT enumAudioEndpoints(EDataFlow dataFlow, WinDef.DWORD dwStateMask, PointerByReference ppDevices);

    @ComMethod(dispId = 2, name = "GetDefaultAudioEndpoint")
    HRESULT getDefaultAudioEndpoint(EDataFlow dataFlow, ERole role, PointerByReference ppEndpoint);

    @ComMethod(dispId = 3, name = "GetDevice")
    HRESULT getDevice(WString pwstrId, PointerByReference ppDevice);

//    @ComMethod(dispId = 4, name = "RegisterEndpointNotificationCallback")
//    HRESULT registerEndpointNotificationCallback(IMMNotificationClient pClient);

//    @ComMethod(dispId = 5, name = "UnregisterEndpointNotificationCallback")
//    HRESULT unregisterEndpointNotificationCallback(IMMNotificationClient pClient);
}
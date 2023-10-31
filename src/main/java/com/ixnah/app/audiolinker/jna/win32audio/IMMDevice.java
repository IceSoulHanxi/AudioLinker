package com.ixnah.app.audiolinker.jna.win32audio;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.COM.util.IUnknown;
import com.sun.jna.platform.win32.COM.util.annotation.ComInterface;
import com.sun.jna.platform.win32.COM.util.annotation.ComMethod;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT.HRESULT;
import com.sun.jna.ptr.PointerByReference;

@ComInterface(iid = "{D666063F-1587-4E43-81F1-B948E807363F}")
public interface IMMDevice extends IUnknown {

    @ComMethod(dispId = 1, name = "Activate")
    HRESULT activate(Guid.REFIID iid, int dwClsCtx, Pointer pActivationParams, PointerByReference ppInterface);

    @ComMethod(dispId = 2, name = "OpenPropertyStore")
    HRESULT openPropertyStore(WinDef.DWORD stgmAccess, PointerByReference ppProperties);

    @ComMethod(dispId = 3, name = "GetId")
    HRESULT getId(PointerByReference ppstrId);

    @ComMethod(dispId = 4, name = "GetState")
    HRESULT getState(WinDef.DWORDByReference pdwState);
}
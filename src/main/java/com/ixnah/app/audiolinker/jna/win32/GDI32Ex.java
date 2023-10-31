package com.ixnah.app.audiolinker.jna.win32;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.W32APIOptions;

public interface GDI32Ex extends GDI32 {

    GDI32Ex INSTANCE = Native.load("gdi32", GDI32Ex.class, W32APIOptions.DEFAULT_OPTIONS);

    int GetPixel(WinDef.HDC hdc, int x, int y);
}

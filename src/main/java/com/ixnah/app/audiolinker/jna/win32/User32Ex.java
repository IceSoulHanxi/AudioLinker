package com.ixnah.app.audiolinker.jna.win32;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.win32.W32APIOptions;

public interface User32Ex extends User32 {

    User32Ex INSTANCE = Native.load("user32", User32Ex.class, W32APIOptions.DEFAULT_OPTIONS);

    boolean DrawFrameControl(HDC hdc, RECT rect, int type, int state);

    int DFC_CAPTION =            1;
    int DFC_MENU =               2;
    int DFC_SCROLL =             3;
    int DFC_BUTTON =             4;
    int DFC_POPUPMENU =          5;

    int DFCS_CAPTIONCLOSE =      0x0000;
    int DFCS_CAPTIONMIN =        0x0001;
    int DFCS_CAPTIONMAX =        0x0002;
    int DFCS_CAPTIONRESTORE =    0x0003;
    int DFCS_CAPTIONHELP =       0x0004;

    int DFCS_INACTIVE =          0x0100;
    int DFCS_PUSHED =            0x0200;
    int DFCS_CHECKED =           0x0400;
    int DFCS_TRANSPARENT =       0x0800;
    int DFCS_HOT =               0x1000;
    int DFCS_ADJUSTRECT =        0x2000;		/* exclude surrounding edge */
    int DFCS_FLAT =              0x4000;
    int DFCS_MONO =              0x8000;
}

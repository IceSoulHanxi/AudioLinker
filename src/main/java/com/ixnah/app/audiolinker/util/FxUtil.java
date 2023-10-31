package com.ixnah.app.audiolinker.util;

import javafx.application.Platform;

public class FxUtil {

    private FxUtil() {
        throw new UnsupportedOperationException();
    }

    public static void runInFxThread(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

}

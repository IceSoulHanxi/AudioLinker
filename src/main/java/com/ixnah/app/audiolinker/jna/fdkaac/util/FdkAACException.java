package com.ixnah.app.audiolinker.jna.fdkaac.util;

public class FdkAACException extends RuntimeException {

    public FdkAACException(final AACEncError error) {
        super(String.format("Error %s returned from calling function", error.name()));
    }
}

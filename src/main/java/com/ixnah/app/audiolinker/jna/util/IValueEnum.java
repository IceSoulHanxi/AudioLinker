package com.ixnah.app.audiolinker.jna.util;

public interface IValueEnum {
    int getValue();

    default boolean equalsValue(Object value) {
        if (value instanceof Integer integer) {
            return integer.equals(getValue());
        }
        return false;
    }
}

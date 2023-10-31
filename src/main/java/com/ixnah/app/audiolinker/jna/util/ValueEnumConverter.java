package com.ixnah.app.audiolinker.jna.util;

import com.sun.jna.FromNativeContext;
import com.sun.jna.ToNativeContext;
import com.sun.jna.TypeConverter;

import java.util.Arrays;

public class ValueEnumConverter implements TypeConverter {

    @Override
    public Object fromNative(Object nativeValue, FromNativeContext context) {
        Class<?> targetType = context.getTargetType();
        if (!targetType.isEnum() || targetType.isAssignableFrom(IValueEnum.class)) return null;
        return Arrays.stream(targetType.getEnumConstants())
                .filter(e -> e instanceof IValueEnum ve && ve.equalsValue(nativeValue))
                .findFirst().orElse(null);
    }

    @Override
    public Object toNative(Object value, ToNativeContext context) {
        return value instanceof IValueEnum ve ? ve.getValue() : null;
    }

    @Override
    public Class<?> nativeType() {
        return Integer.class;
    }
}

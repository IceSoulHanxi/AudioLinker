package com.ixnah.app.audiolinker.util;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

@SuppressWarnings("all")
public class UnsafeUtil {

    private UnsafeUtil() {
        throw new UnsupportedOperationException();
    }

    private static final sun.misc.Unsafe unsafe;

    static {
        try {
            Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (sun.misc.Unsafe) theUnsafe.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T getStatic(Class<?> cl, String name) {
        try {
            ensureClassInitialized(cl);
            Field field = cl.getDeclaredField(name);
            Object materialByNameBase = unsafe.staticFieldBase(field);
            long materialByNameOffset = unsafe.staticFieldOffset(field);
            return (T) unsafe.getObject(materialByNameBase, materialByNameOffset);
        } catch (Exception e) {
            return null;
        }
    }

    private static void ensureClassInitialized(Class<?> c) {
        try {
            MethodHandles.lookup().ensureInitialized(c);
        } catch (IllegalAccessException e) {
        }
    }
}

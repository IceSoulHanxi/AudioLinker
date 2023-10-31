package com.ixnah.app.audiolinker.util;

import com.sun.jna.Function;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.COM.util.ComThread;
import com.sun.jna.platform.win32.COM.util.IComEnum;
import com.sun.jna.platform.win32.COM.util.annotation.ComInterface;
import com.sun.jna.platform.win32.COM.util.annotation.ComMethod;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.ptr.PointerByReference;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static com.sun.jna.Function.ALT_CONVENTION;
import static com.sun.jna.Pointer.NULL;
import static com.sun.jna.platform.win32.Ole32.COINIT_MULTITHREADED;

public class ComUtil {

    private ComUtil() {
        throw new UnsupportedOperationException();
    }

    static {
        COMUtils.checkRC(Ole32.INSTANCE.CoInitializeEx(NULL, COINIT_MULTITHREADED));
    }

    private static final ComThread comThread = new ComThread("Default COM Thread", 5000, (t, e) -> {
    });

    public static void runInComThread(Runnable runnable) {
        runInComThread((Callable<Void>) () -> {
            runnable.run();
            return null;
        });
    }

    public static <T> T runInComThread(Callable<T> callable) {
        try {
            return comThread.execute(callable);
        } catch (TimeoutException | InterruptedException ex) {
            throw new RuntimeException(ex);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException) {
                appendStacktrace(ex, cause);
                throw (RuntimeException) cause;
            } else if (cause instanceof InvocationTargetException) {
                cause = ((InvocationTargetException) cause).getTargetException();
                if (cause instanceof RuntimeException) {
                    appendStacktrace(ex, cause);
                    throw (RuntimeException) cause;
                }
            }
            throw new RuntimeException(ex);
        }
    }

    /**
     * Append the stack trace available via caughtException to the stack trace
     * of toBeThrown. The combined stack trace is reassigned to toBeThrown
     */
    private static void appendStacktrace(Exception caughtException, Throwable toBeThrown) {
        StackTraceElement[] upperTrace = caughtException.getStackTrace();
        StackTraceElement[] lowerTrace = toBeThrown.getStackTrace();
        StackTraceElement[] trace = new StackTraceElement[upperTrace.length + lowerTrace.length];
        System.arraycopy(upperTrace, 0, trace, lowerTrace.length, upperTrace.length);
        System.arraycopy(lowerTrace, 0, trace, 0, lowerTrace.length);
        toBeThrown.setStackTrace(trace);
    }

    public static ComThread getComThread() {
        return comThread;
    }

    public static Guid.IID getIid(Class<?> tClass) {
        ComInterface iface = tClass.getAnnotation(ComInterface.class);
        return  iface != null ? new Guid.IID(iface.iid()) : Guid.IID_NULL;
    }

    @SuppressWarnings("unchecked")
    public static <T> T newProxy(PointerByReference ppv, T... reified) {
        Class<?> iface = reified.getClass().getComponentType();
        return (T) Proxy.newProxyInstance(ComUtil.class.getClassLoader(), new Class[]{iface}, new ComProxy(ppv.getValue()));
    }

    @SuppressWarnings("unchecked")
    public static <T> T newProxy(Pointer pointer, T... reified) {
        Class<?> iface = reified.getClass().getComponentType();
        return (T) Proxy.newProxyInstance(ComUtil.class.getClassLoader(), new Class[]{iface}, new ComProxy(pointer));
    }

    public static void releaseProxy(Object comObject) {
        if (comObject == null) return;
        if (!Proxy.isProxyClass(comObject.getClass())) return;
        InvocationHandler handler = Proxy.getInvocationHandler(comObject);
        if (handler instanceof ComProxy comProxy) {
            Pointer vTable = comProxy.pointer.getPointer(0);
            Pointer funcPointer = vTable.getPointer(2L * Native.POINTER_SIZE);
            Function function = Function.getFunction(funcPointer, ALT_CONVENTION);
            function.invokeLong(new Object[]{comProxy.pointer});
        }
    }

    public static void releaseStruct(Structure struct) {
        Ole32.INSTANCE.CoTaskMemFree(struct.getPointer());
    }

    private static class ComProxy implements InvocationHandler {

        final Pointer pointer;

        private ComProxy(Pointer pointer) {
            this.pointer = pointer;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            ComMethod comMethod = method.getAnnotation(ComMethod.class);
            if (comMethod == null) return null;
            Class<?> returnType = method.getReturnType();
            Object[] fullLengthArgs = unfoldWhenVarargs(method, pointer, args);
            int dispId = comMethod.dispId();
            if (dispId != -1) {
                Pointer vTable = pointer.getPointer(0);
                Pointer funcPointer = vTable.getPointer((dispId + 2L) * Native.POINTER_SIZE);
                Function function = Function.getFunction(funcPointer, ALT_CONVENTION);
                return runInComThread(() -> {
                    if (Integer.TYPE.equals(returnType)) {
                        return function.invokeInt(fullLengthArgs);
                    } else if (Long.TYPE.equals(returnType)) {
                        return function.invokeLong(fullLengthArgs);
                    } else if (Double.TYPE.equals(returnType)) {
                        return function.invokeDouble(fullLengthArgs);
                    } else if (Float.TYPE.equals(returnType)) {
                        return function.invokeFloat(fullLengthArgs);
                    }
                    return function.invoke(returnType, fullLengthArgs);
                });
            } /*else {
                String methName = this.getMethodName(method, meth);
                return this.invokeMethod(returnType, methName, fullLengthArgs);
            }*/
            return null;
        }

        private Object[] unfoldWhenVarargs(Method method, Pointer baseAddress, Object[] argParams) {
            if (null == argParams) {
                return new Object[]{baseAddress};
            }
            Stream<Object> argStream;
            if (argParams.length == 0 || !method.isVarArgs() || !(argParams[argParams.length - 1] instanceof Object[] varargs)) {
                argStream = Stream.concat(Stream.of(baseAddress), Arrays.stream(argParams));
            } else {
                argStream = Stream.concat(Stream.of(baseAddress), Stream.concat(Arrays.stream(argParams).limit(argParams.length - 1), Arrays.stream(varargs)));
            }
            return argStream.map(param -> param instanceof IComEnum comEnum ? comEnum.getValue() : param).toArray();
        }
    }
}

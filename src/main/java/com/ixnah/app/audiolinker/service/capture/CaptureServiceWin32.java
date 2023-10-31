package com.ixnah.app.audiolinker.service.capture;

import com.ixnah.app.audiolinker.jna.win32audio.IAudioCaptureClient;
import com.ixnah.app.audiolinker.jna.win32audio.IAudioClient;
import com.ixnah.app.audiolinker.jna.win32audio.IMMDevice;
import com.ixnah.app.audiolinker.jna.win32audio.IMMDeviceEnumerator;
import com.ixnah.app.audiolinker.jna.win32audio.structure.WAVEFORMATEX;
import com.ixnah.app.audiolinker.jna.win32audio.util.BufferFlags;
import com.ixnah.app.audiolinker.util.ComUtil;
import com.ixnah.app.audiolinker.jna.win32audio.util.EDataFlow;
import com.ixnah.app.audiolinker.jna.win32audio.util.ERole;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;

import static com.sun.jna.Pointer.NULL;
import static com.sun.jna.platform.win32.COM.COMUtils.checkRC;
import static com.sun.jna.platform.win32.WTypes.CLSCTX_ALL;

public class CaptureServiceWin32 implements CaptureService {

    private static final Guid.CLSID CLSID_MMDeviceEnumerator = new Guid.CLSID("BCDE0395-E52F-467C-8E3D-C4579291692E");
    private static final int AUDCLNT_SHAREMODE_SHARED = 0;
    private static final int AUDCLNT_STREAMFLAGS_LOOPBACK = 0x00020000;
    private static final AtomicInteger THREAD_ID = new AtomicInteger(1);

    private final List<Function<ByteBuffer, CompletableFuture<?>>> listeners = new CopyOnWriteArrayList<>();
    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicBoolean capturing = new AtomicBoolean();
    private final IMMDeviceEnumerator enumerator;
    private final IMMDevice device;
    private final IAudioClient audioClient;
    private final WAVEFORMATEX.ByReference pDeviceFormat;
    private final IAudioCaptureClient captureClient;
    private final long streamLatency;
    private final long bufferSize;
    private final ExecutorService captureThread;
    private final long hnsBufferDuration;

    CaptureServiceWin32(long hnsBufferDuration) {
        PointerByReference ppEnumerator = new PointerByReference();
        checkRC(Ole32.INSTANCE.CoCreateInstance(CLSID_MMDeviceEnumerator, null, CLSCTX_ALL, ComUtil.getIid(IMMDeviceEnumerator.class), ppEnumerator));
        enumerator = ComUtil.newProxy(ppEnumerator);

        PointerByReference ppDevice = new PointerByReference();
        checkRC(enumerator.getDefaultAudioEndpoint(EDataFlow.eRender, ERole.eConsole, ppDevice));
        device = ComUtil.newProxy(ppDevice);

        PointerByReference ppInterface = new PointerByReference();
        checkRC(device.activate(new Guid.REFIID(ComUtil.getIid(IAudioClient.class)), CLSCTX_ALL, NULL, ppInterface));
        audioClient = ComUtil.newProxy(ppInterface);

        PointerByReference ppDeviceFormat = new PointerByReference();
        checkRC(audioClient.getMixFormat(ppDeviceFormat));
        pDeviceFormat = new WAVEFORMATEX.ByReference(ppDeviceFormat.getValue());

        checkRC(audioClient.initialize(AUDCLNT_SHAREMODE_SHARED, AUDCLNT_STREAMFLAGS_LOOPBACK, hnsBufferDuration, 0, pDeviceFormat, null));

        PointerByReference ppv = new PointerByReference();
        checkRC(audioClient.getService(ComUtil.getIid(IAudioCaptureClient.class), ppv));
        captureClient = ComUtil.newProxy(ppv);

        WinDef.LONGLONGByReference pLatency = new WinDef.LONGLONGByReference();
        checkRC(audioClient.getStreamLatency(pLatency));
        streamLatency = pLatency.getValue().longValue();

        WinDef.LONGLONGByReference phnsDefaultDevicePeriod = new WinDef.LONGLONGByReference();
        WinDef.LONGLONGByReference phnsMinimumDevicePeriod = new WinDef.LONGLONGByReference();
        checkRC(audioClient.getDevicePeriod(phnsDefaultDevicePeriod, phnsMinimumDevicePeriod));

        WinDef.UINTByReference pBufferSize = new WinDef.UINTByReference();
        checkRC(audioClient.getBufferSize(pBufferSize));
        bufferSize = pBufferSize.getValue().longValue();

        captureThread = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "Audio Capture Thread - " + THREAD_ID.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        });

        this.hnsBufferDuration = hnsBufferDuration;
    }

    @Override
    public int getSimpleRate() {
        return pDeviceFormat.nSamplesPerSec;
    }

    @Override
    public int getChannelCount() {
        return pDeviceFormat.nChannels;
    }

    @Override
    public int getSampleBits() {
        return pDeviceFormat.wBitsPerSample;
    }

    @Override
    public int getFrameSize() {
        return pDeviceFormat.nBlockAlign;
    }

    @Override
    public long getBufferSize() {
        return bufferSize;
    }

    @Override
    public long getStreamLatency() {
        return streamLatency;
    }

    @Override
    public int addListener(Function<ByteBuffer, CompletableFuture<?>> listener) {
        listeners.add(listener);
        return listeners.lastIndexOf(listener);
    }

    @Override
    public void removeListener(int listenerId) {
        listeners.remove(listenerId);
    }

    @Override
    public boolean capturing() {
        return capturing.get() && !captureThread.isShutdown();
    }

    @Override
    public boolean stopped() {
        return started.get() && captureThread.isShutdown();
    }

    @Override
    public void start() {
        if (started.get()) return;
        captureThread.execute(() -> {
            audioClient.start();

            WinDef.UINTByReference pNumPaddingFrames = new WinDef.UINTByReference();
            audioClient.getCurrentPadding(pNumPaddingFrames);
            long numPaddingFrames = pNumPaddingFrames.getValue().longValue();

            double hnsActualDuration = hnsBufferDuration * bufferSize / (double) pDeviceFormat.nSamplesPerSec;
            capturing.set(true);

            while (capturing.get()) {
                LockSupport.parkNanos(100 * (long) hnsActualDuration / 2);

                IntByReference pNextPackageSize = new IntByReference();
                checkRC(captureClient.getNextPacketSize(pNextPackageSize));
                while (pNextPackageSize.getValue() > 0) {
                    PointerByReference ppData = new PointerByReference();
                    IntByReference pNumFramesAvailable = new IntByReference();
                    IntByReference pFlags = new IntByReference();
                    checkRC(captureClient.getBuffer(ppData, pNumFramesAvailable, pFlags, null, null));

                    long silentFlag = BufferFlags.Silent.getValue();
                    int numFramesAvailable = pNumFramesAvailable.getValue();
                    ByteBuffer byteBuffer;
                    if ((pFlags.getValue() & silentFlag) == silentFlag) {
                        byteBuffer = ByteBuffer.allocateDirect((int) (numPaddingFrames * getFrameSize()));
                        while (byteBuffer.hasRemaining()) {
                            byteBuffer.put((byte) 0);
                        }
                    } else {
                        byteBuffer = ppData.getValue().getByteBuffer(0, (long) numFramesAvailable * getFrameSize());
                    }

                    CompletableFuture.allOf(listeners.stream().map(f -> f.apply(byteBuffer)).toArray(CompletableFuture[]::new)).join();

                    if (numPaddingFrames != 0) {
                        checkRC(captureClient.releaseBuffer(numFramesAvailable));
                    }

                    checkRC(captureClient.getNextPacketSize(pNextPackageSize));
                }
            }
        });
        started.set(true);
    }

    @Override
    public void stop() {
        capturing.set(false);
        if (!captureThread.isShutdown()) {
            captureThread.shutdownNow();
        }
        ComUtil.releaseProxy(captureClient);
        ComUtil.releaseStruct(pDeviceFormat);
        ComUtil.releaseProxy(audioClient);
        ComUtil.releaseProxy(device);
        ComUtil.releaseProxy(enumerator);
    }
}

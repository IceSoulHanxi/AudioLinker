package com.ixnah.app.audiolinker;

import com.ixnah.app.audiolinker.jna.win32audio.*;
import com.ixnah.app.audiolinker.jna.win32audio.util.BufferFlags;
import com.ixnah.app.audiolinker.jna.win32audio.util.EDataFlow;
import com.ixnah.app.audiolinker.jna.win32audio.util.ERole;
import com.ixnah.app.audiolinker.jna.win32audio.structure.WAVEFORMATEX;
import com.ixnah.app.audiolinker.util.ComUtil;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static com.sun.jna.Pointer.NULL;
import static com.sun.jna.platform.win32.COM.COMUtils.checkRC;
import static com.sun.jna.platform.win32.Ole32.COINIT_MULTITHREADED;
import static com.sun.jna.platform.win32.WTypes.CLSCTX_ALL;

public class AudioCaptureTest {

    private static final Logger LOG = LoggerFactory.getLogger(AudioCaptureTest.class);

    private static final Guid.CLSID CLSID_MMDeviceEnumerator = new Guid.CLSID("BCDE0395-E52F-467C-8E3D-C4579291692E");
    private static final Guid.IID IID_IMMDeviceEnumerator = new Guid.IID("A95664D2-9614-4F35-A746-DE8DB63617E6");
    private static final Guid.IID IID_IAudioClient = new Guid.IID("1CB9AD4C-DBFA-4c32-B178-C2F568A703B2");
    private static final Guid.IID IID_IAudioCaptureClient = new Guid.IID("C8ADBD64-E71E-48a0-A4DE-185C395CD317");
    private static final int AUDCLNT_SHAREMODE_SHARED = 0;
    private static final int AUDCLNT_STREAMFLAGS_LOOPBACK = 0x00020000;


    public static void main(String[] args) {
        Ole32 ole32 = Ole32.INSTANCE;
        checkRC(ole32.CoInitializeEx(NULL, COINIT_MULTITHREADED));

        PointerByReference ppEnumerator = new PointerByReference();
        checkRC(Ole32.INSTANCE.CoCreateInstance(CLSID_MMDeviceEnumerator, null, CLSCTX_ALL, IID_IMMDeviceEnumerator, ppEnumerator));
        Pointer value = ppEnumerator.getValue();
        LOG.info("pEnumerator: " + value);
        IMMDeviceEnumerator enumerator = ComUtil.newProxy(ppEnumerator);
        PointerByReference ppDevice = new PointerByReference();
        checkRC(enumerator.getDefaultAudioEndpoint(EDataFlow.eRender, ERole.eConsole, ppDevice));
        IMMDevice device = ComUtil.newProxy(ppDevice);

        PointerByReference ppInterface = new PointerByReference();
        checkRC(device.activate(new Guid.REFIID(IID_IAudioClient), CLSCTX_ALL, NULL, ppInterface));
        IAudioClient audioClient = ComUtil.newProxy(ppInterface);

        PointerByReference ppDeviceFormat = new PointerByReference();
        checkRC(audioClient.getMixFormat(ppDeviceFormat));
        WAVEFORMATEX.ByReference pDeviceFormat = new WAVEFORMATEX.ByReference(ppDeviceFormat.getValue());
        LOG.info("""
                
                wFormatTag: {}
                nChannels: {}
                nSamplesPerSec: {}
                nAvgBytesPerSec: {}
                nBlockAlign: {}
                wBitsPerSample: {}
                cbSize: {}
                """,
                pDeviceFormat.wFormatTag.intValue(), pDeviceFormat.nChannels, pDeviceFormat.nSamplesPerSec,
                pDeviceFormat.nAvgBytesPerSec, pDeviceFormat.nBlockAlign, pDeviceFormat.wBitsPerSample,
                pDeviceFormat.cbSize);

//        long hnsRequestedDuration = TimeUnit.SECONDS.toNanos(1) / 100;
        long hnsRequestedDuration = TimeUnit.MILLISECONDS.toNanos(20) / 100;
        LOG.info("hnsRequestedDuration: {}", hnsRequestedDuration);
        checkRC(audioClient.initialize(AUDCLNT_SHAREMODE_SHARED, AUDCLNT_STREAMFLAGS_LOOPBACK, hnsRequestedDuration, 0, pDeviceFormat, null));

        WinDef.LONGLONGByReference pLatency = new WinDef.LONGLONGByReference();
        checkRC(audioClient.getStreamLatency(pLatency));
        LOG.info("StreamLatency: {}", pLatency.getValue().longValue());

        WinDef.LONGLONGByReference hnsDefaultDevicePeriod = new WinDef.LONGLONGByReference();
        WinDef.LONGLONGByReference hnsMinimumDevicePeriod = new WinDef.LONGLONGByReference();
        checkRC(audioClient.getDevicePeriod(hnsDefaultDevicePeriod, hnsMinimumDevicePeriod));
        LOG.info("""
                
                hnsDefaultDevicePeriod: {}
                hnsMinimumDevicePeriod: {}
                """, hnsDefaultDevicePeriod.getValue().longValue(), hnsMinimumDevicePeriod.getValue().longValue());

        WinDef.UINTByReference pBufferSize = new WinDef.UINTByReference();
        checkRC(audioClient.getBufferSize(pBufferSize));
        LOG.info("BufferSize: {}", pBufferSize.getValue().longValue());

        PointerByReference ppv = new PointerByReference();
        checkRC(audioClient.getService(IID_IAudioCaptureClient, ppv));
        IAudioCaptureClient captureClient = ComUtil.newProxy(ppv);
        checkRC(audioClient.start());

        LOG.info("开始采集音频");
        double hnsActualDuration = hnsRequestedDuration * pBufferSize.getValue().longValue() / (double) pDeviceFormat.nSamplesPerSec;
        LOG.info("hnsActualDuration: {}", hnsActualDuration);
        for (int i = 0; i < 10000; i++) {
            LockSupport.parkNanos(100L * (long) hnsActualDuration / 2L);

            IntByReference pNextPackageSize = new IntByReference();
            checkRC(captureClient.getNextPacketSize(pNextPackageSize));
            LOG.info("nextPackageSize: {}", pNextPackageSize.getValue());
            while (pNextPackageSize.getValue() > 0) {
                PointerByReference ppData = new PointerByReference();
                IntByReference pNumFramesAvailable = new IntByReference();
                IntByReference pFlags = new IntByReference();
                checkRC(captureClient.getBuffer(ppData, pNumFramesAvailable, pFlags, null, null));
                LOG.info("pFlags: {}", pFlags.getValue());

                ByteBuf byteBuffer;
                long silentFlag = BufferFlags.Silent.getValue();
                if ((pFlags.getValue() & silentFlag) == silentFlag) {
                    byteBuffer = Unpooled.EMPTY_BUFFER;
                } else {
                    byteBuffer = Unpooled.wrappedBuffer(ppData.getValue().getByteBuffer(0, (long) pNumFramesAvailable.getValue() * pDeviceFormat.nBlockAlign));
                }
                LOG.info("byteBuffer.Size: {}", byteBuffer.readableBytes());
                LOG.info(ByteBufUtil.hexDump(Unpooled.wrappedBuffer(byteBuffer)));

                checkRC(captureClient.releaseBuffer(pNumFramesAvailable.getValue()));

                checkRC(captureClient.getNextPacketSize(pNextPackageSize));
            }
        }

        ole32.CoUninitialize();
    }
}
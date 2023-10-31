package com.ixnah.app.audiolinker.service.capture;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface CaptureService extends AutoCloseable {

    static CaptureService create(long hnsBufferDuration) {
        return new CaptureServiceWin32(hnsBufferDuration);
    }

    int getSimpleRate();

    int getChannelCount();

    int getSampleBits();

    int getFrameSize();

    long getBufferSize();

    long getStreamLatency();

    int addListener(Function<ByteBuffer, CompletableFuture<?>> listener);

    void removeListener(int listenerId);

    boolean capturing();

    boolean stopped();

    void start();

    void stop() throws Exception;

    @Override
    default void close() throws Exception {
        stop();
    }
}

package com.ixnah.app.audiolinker.util;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.CompletableFuture;

public class NettyUtil {

    private NettyUtil() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public static <T> CompletableFuture<T> toCompletable(Future<T> future) {
        CompletableFuture<T> completable = new CompletableFuture<>();

        future.addListener(netty -> {
            if (netty.isSuccess()) {
                completable.complete((T) netty.getNow());
            } else {
                completable.completeExceptionally(netty.cause());
            }
        });

        return completable.whenComplete((r, t) -> {
            if (completable.isCancelled() && future.isCancellable() && !future.isCancelled()) {
                future.cancel(true);
            }
        });
    }
}

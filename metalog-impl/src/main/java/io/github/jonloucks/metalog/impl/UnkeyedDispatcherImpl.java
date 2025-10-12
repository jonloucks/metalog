package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.AutoOpen;
import io.github.jonloucks.metalog.api.Meta;
import io.github.jonloucks.metalog.api.Metalog;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;

import static io.github.jonloucks.metalog.impl.Internal.commandCheck;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

final class UnkeyedDispatcherImpl implements Dispatcher, AutoOpen {

    @Override
    public AutoClose open() {
        if (idempotent.transitionToOpened()) {
            return this::close;
        } else {
            // Possibly impossible to happen, but a good practice
            return ()->{};
        }
    }
    
    @Override
    public void dispatch(Meta meta, Runnable command) {
        final Runnable validCommand = commandCheck(command);
        if (idempotent.isRejecting()) {
            validCommand.run();
            return;
        }
        this.executor.execute(validCommand);
    }
    
    UnkeyedDispatcherImpl(Metalog.Config config) {
        this.config = config;
        this.executor = new ThreadPoolExecutor(
            1,
            config.unkeyedThreadCount(),
            60L,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(config.unkeyedFairness())
        );
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    }
    
    private void close() {
        if (idempotent.transitionToClosing()) {
            realClosing();
        }
    }
    
    private void realClosing() {
        final Instant start = Instant.now();
        try {
            boolean hasForcedShutdown = false;
            
            initiateShutdown();
            
            while (!checkIfShutdown()) {
                final Duration duration = Duration.between(start, Instant.now());
                if (hasForcedShutdown) {
                    if (isTimeToGiveUp(duration)) {
                        return;
                    }
                } else if (isTimeToForceShutdown(duration)) {
                    forceShutdown();
                    hasForcedShutdown = true;
                }
            }
        } finally {
            idempotent.transitionToClosed();
        }
    }
    
    private boolean isTimeToGiveUp(Duration duration) {
        return duration.compareTo(config.shutdownTimeout()) > 0;
    }
    
    private boolean isTimeToForceShutdown(Duration duration) {
        final Duration shutdownTime = config.shutdownTimeout();
        return duration.compareTo(shutdownTime.dividedBy(2)) > 0;
    }
    
    private void initiateShutdown() {
        executor.shutdown();
    }
    
    private boolean checkIfShutdown() {
        try {
            return executor.awaitTermination(1, MILLISECONDS);
        } catch (InterruptedException thrown) {
            return executor.isShutdown();
        }
    }
    
    private void forceShutdown() {
        executor.shutdownNow();
    }

    private final IdempotentImpl idempotent = new IdempotentImpl();
    private final Metalog.Config config;
    private final ThreadPoolExecutor executor;
}

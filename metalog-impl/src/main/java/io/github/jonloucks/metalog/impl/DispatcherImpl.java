package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.AutoOpen;
import io.github.jonloucks.metalog.api.Metalog;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

final class DispatcherImpl implements Dispatcher, AutoOpen, AutoClose {
    
    @Override
    public void execute(Runnable command) {
        executor.execute(command);
    }
    
    @Override
    public AutoClose open() {
        if (openState.transitionToOpen()) {
            if (config.backlogThreadCount() <= 0) {
                executor = Runnable::run;
            } else if (config.backlogThreadCount() > 1) {
                executor = Executors.newFixedThreadPool(config.backlogThreadCount());
            } else {
                executor = Executors.newSingleThreadExecutor();
            }
            return this;
        } else {
            return () -> {};
        }
    }
    
    @Override
    public void close() {
        if (openState.transitionToClosed()) {
            try {
                if (executor instanceof ExecutorService) {
                    final ExecutorService executorService = (ExecutorService) executor;
                    final long millis = Math.max(1, config.shutdownTimeout().toMillis()/2);
                    executorService.shutdown();
                    try {
                        if (!executorService.awaitTermination(millis, MILLISECONDS)) {
                            executorService.shutdownNow();
                            if (!executorService.awaitTermination(millis, MILLISECONDS)) {
                                System.err.println("Metalog dispatcher failed to shutdown");
                            }
                        }
                    } catch (InterruptedException ex) {
                        executorService.shutdownNow();
                        Thread.currentThread().interrupt();
                    }
                }
            } finally {
                executor = null;
            }
        }
    }
    
    DispatcherImpl(Metalog.Config config) {
        this.config = config;
    }
    
    private final Metalog.Config config;
    private final IdempotentImpl openState = new IdempotentImpl();
    private Executor executor = null;
}

package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.AutoOpen;
import io.github.jonloucks.metalog.api.Meta;
import io.github.jonloucks.metalog.api.Metalog;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.SECONDS;

final class DispatcherImpl implements Dispatcher, AutoOpen {
    
    @Override
    public void dispatch(Meta meta, Runnable command) {
        if (!idempotent.isActive()) {
            throw new IllegalStateException("Metalog Dispatcher must be active.");
        }
        final Optional<String> optionalKey = meta.getKey();
        if (optionalKey.isPresent()) {
            final String key = optionalKey.get();
            final int assignment = key.hashCode() % config.keyedThreadCount();
        } else {
            executor.execute(command);
        }
    }
    
    @Override
    public AutoClose open() {
        if (idempotent.transitionToOpened()) {
            if (config.keyedThreadCount() <= 0) {
                executor = Runnable::run;
            } else if (config.keyedThreadCount() > 1) {
                executor = Executors.newFixedThreadPool(config.keyedThreadCount());
            } else {
                executor = Executors.newSingleThreadExecutor();
            }
            executors.add(executor);
            return this::close;
        } else {
            return () -> {}; // all open calls after the first get a do nothing close
        }
    }
    
    private void close() {
        if (idempotent.transitionToClosing()) {
            realClose();
        }
    }
    
    // for each sub executor call shutdown
    // for each sub executor wait a small amount of time for shutdown
    // for each not shutdown call shutdownNow
    // periodically check if done.
    // timeout when total timeout time has been exceeded
    
    DispatcherImpl(Metalog.Config config) {
        this.config = config;
    }
    
    private void realClose() {
        final Instant start = Instant.now();
        try {
            boolean hasForcedShutdown = false;
            
            initiateShutdown();
            
            while (!checkIfShutdown()) {
                final Duration duration = Duration.between(start, Instant.now());
                if (duration.compareTo(config.shutdownTimeout().dividedBy(2)) > 0 && !hasForcedShutdown) {
                    hasForcedShutdown = true;
                    forceShutdown();
                } else if (duration.compareTo(config.shutdownTimeout()) > 0) {
                    System.err.println("Metalog dispatcher failed to shutdown.");
                    return;
                }
            }
        } finally {
            idempotent.transitionToClosed();
            executors.clear();
        }
    }
    
    private void initiateShutdown() {
        executors.forEach(ifExecutorService(ExecutorService::shutdown));
    }
    
    private Consumer<Executor> ifExecutorService(Consumer<ExecutorService> consumer) {
        return executor -> {
            if (executor instanceof ExecutorService) {
                consumer.accept((ExecutorService) executor);
            }
        };
    }
 
    private boolean checkIfShutdown() {
        final AtomicInteger notShutdownCounter = new AtomicInteger();
        executors.forEach(ifExecutorService(executorService -> {
            try {
                if (!executorService.awaitTermination(1, SECONDS)) {
                    notShutdownCounter.incrementAndGet();
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }));
        return notShutdownCounter.get() == 0;
    }
    
    private void forceShutdown() {
        executors.forEach(ifExecutorService(ExecutorService::shutdownNow));
    }
    
    // for each sub executor wait a small amount of time for shutdown
    // for each not shutdown call shutdownNow
    // periodically check if done.
    // timeout when total timeout time has been exceeded
    private final Metalog.Config config;
    private final IdempotentImpl idempotent = new IdempotentImpl();
    private Executor executor = null;
    private final List<Executor> executors = new ArrayList<>();
}

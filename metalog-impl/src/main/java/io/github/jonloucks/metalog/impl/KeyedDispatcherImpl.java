package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.AutoOpen;
import io.github.jonloucks.metalog.api.Meta;
import io.github.jonloucks.metalog.api.Metalog;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

import static io.github.jonloucks.contracts.api.Checks.nullCheck;

final class KeyedDispatcherImpl implements Dispatcher, AutoOpen {
    @Override
    public AutoClose open() {
        if (idempotent.transitionToOpened()) {
            thread.start();
            return this::close;
        } else {
            return ()->{};
        }
    }
    
    @Override
    public void dispatch(Meta meta, Runnable command) {
        final Runnable validCommand = nullCheck(command, "Command must be present.");
        if (idempotent.isRejecting()) {
            throw new IllegalStateException("Executor has been closed.");
        }
        semaphore.acquireUninterruptibly();
        try {
            queue.put(validCommand);
        } catch (InterruptedException e) {
            semaphore.release();
        }
    }
    
    KeyedDispatcherImpl(Metalog.Config config) {
        this.semaphore = new Semaphore(config.keyedQueueLimit());
        this.queue = new ArrayBlockingQueue<>(config.keyedQueueLimit());
        this.thread = new Thread(this::consumeLoop);
    }
    
    private void close() {
        if (idempotent.transitionToClosed()) {
            thread.interrupt();
            semaphore.release(queue.size());
            queue.clear();
        }
    }
    
    private void consumeLoop() {
        while (idempotent.isActive()) {
            try {
                final Runnable command = queue.take();
                try {
                    command.run();
                } finally {
                    semaphore.release();
                }
            }  catch (InterruptedException ignored) {
            }
        }
    }
    
    private final IdempotentImpl idempotent = new IdempotentImpl();
    private final Semaphore semaphore;
    private final Thread thread;
    private final ArrayBlockingQueue<Runnable> queue;
}

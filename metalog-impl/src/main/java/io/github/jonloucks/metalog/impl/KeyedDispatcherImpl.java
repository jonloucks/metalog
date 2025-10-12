package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.AutoOpen;
import io.github.jonloucks.metalog.api.Meta;
import io.github.jonloucks.metalog.api.Metalog;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

import static io.github.jonloucks.metalog.impl.Internal.commandCheck;

final class KeyedDispatcherImpl implements Dispatcher, AutoOpen {
    
    @Override
    public AutoClose open() {
        if (idempotent.transitionToOpening()) {
            return realOpening();
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
        inflightSemaphore.acquireUninterruptibly();
        try {
            workQueue.put(validCommand);
        } catch (InterruptedException ignored) {
            inflightSemaphore.release();
        }
    }
    
    KeyedDispatcherImpl(Metalog.Config config) {
        this.inflightSemaphore = new Semaphore(config.keyedQueueLimit());
        this.workQueue = new ArrayBlockingQueue<>(config.keyedQueueLimit());
        this.workerThread = new Thread(this::consumeLoop);
    }
    
    private AutoClose realOpening() {
        workerThread.start();
        idempotent.transitionToOpened();
        return this::close;
    }
    
    private void close() {
        if (idempotent.transitionToClosed()) {
            workerThread.interrupt();
            inflightSemaphore.release(workQueue.size());
            workQueue.clear();
        }
    }
    
    private void consumeLoop() {
        while (idempotent.isActive()) {
            try {
                final Runnable command = workQueue.take();
                try {
                    command.run();
                } finally {
                    inflightSemaphore.release();
                }
            }  catch (InterruptedException ignored) {
            }
        }
    }
    
    private final IdempotentImpl idempotent = new IdempotentImpl();
    private final Semaphore inflightSemaphore;
    private final Thread workerThread;
    private final ArrayBlockingQueue<Runnable> workQueue;
}

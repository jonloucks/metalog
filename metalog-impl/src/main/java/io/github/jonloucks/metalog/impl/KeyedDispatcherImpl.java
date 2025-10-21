package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.concurrency.api.Idempotent;
import io.github.jonloucks.concurrency.api.StateMachine;
import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.AutoOpen;
import io.github.jonloucks.metalog.api.Dispatcher;
import io.github.jonloucks.metalog.api.Meta;
import io.github.jonloucks.metalog.api.Metalog;
import io.github.jonloucks.metalog.api.Outcome;

import java.time.Duration;
import java.util.concurrent.*;

import static io.github.jonloucks.concurrency.api.Idempotent.withClose;
import static io.github.jonloucks.concurrency.api.Idempotent.withOpen;
import static io.github.jonloucks.metalog.impl.Internal.commandCheck;
import static io.github.jonloucks.metalog.impl.Internal.runWithIgnore;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

final class KeyedDispatcherImpl implements Dispatcher, AutoOpen {
    
    @Override
    public AutoClose open() {
        return withOpen(stateMachine, this::realOpen);
    }
    
    @Override
    public Outcome dispatch(Meta meta, Runnable work) {
        final Runnable validCommand = commandCheck(work);
        if (stateMachine.getState().isRejecting()) {
            validCommand.run();
            return Outcome.CONSUMED;
        }
        inflightSemaphore.acquireUninterruptibly();
        try {
            workQueue.put(validCommand);
        } catch (InterruptedException ignored) {
            inflightSemaphore.release();
        }
        return Outcome.DISPATCHED;
    }
    
    KeyedDispatcherImpl(Metalog.Config config) {
        this.inflightSemaphore = new Semaphore(config.keyedQueueLimit());
        this.workQueue = new ArrayBlockingQueue<>(config.keyedQueueLimit());
        this.workerThread = new Thread(this::consumeLoop);
        this.shutdownTimeout = config.shutdownTimeout();
        this.stateMachine = Idempotent.createStateMachine(config.contracts());
    }
    
    private AutoClose realOpen() {
        workerThread.start();
        return this::close;
    }
    
    private void close() {
        withClose(stateMachine, this::realClose);
    }
    
    private void realClose() {
        triggerWorkerExit.countDown();
        runWithIgnore(() -> {
            if (!workerExitedLatch.await(shutdownTimeout.toMillis(), MILLISECONDS)) {
                workerThread.interrupt();
            }
        });
  
        // taking over emptying the queue
        while (!workQueue.isEmpty()) {
            runQueueJob(workQueue.poll());
        }
    }
    
    private void runQueueJob(Runnable command) {
        if (ofNullable(command).isPresent()) {
            try {
                runWithIgnore(command::run);
            } finally {
                inflightSemaphore.release();
            }
        }
    }
    
    private void consumeLoop() {
        try {
            runWithIgnore(() -> {
                while (!triggerWorkerExit.await(1, MILLISECONDS)) {
                    runQueueJob(workQueue.poll(1, MILLISECONDS));
                }
            });

        } finally {
            workerExitedLatch.countDown();
        }
    }
    
    private final StateMachine<Idempotent> stateMachine;
    private final Duration shutdownTimeout;
    private final Semaphore inflightSemaphore;
    private final Thread workerThread;
    private final ArrayBlockingQueue<Runnable> workQueue;
    private final CountDownLatch triggerWorkerExit = new CountDownLatch(1);
    private final CountDownLatch workerExitedLatch = new CountDownLatch(1);
}

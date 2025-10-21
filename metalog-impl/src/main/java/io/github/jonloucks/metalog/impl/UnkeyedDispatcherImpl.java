package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.concurrency.api.Idempotent;
import io.github.jonloucks.concurrency.api.StateMachine;
import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.AutoOpen;
import io.github.jonloucks.metalog.api.Dispatcher;
import io.github.jonloucks.metalog.api.Meta;
import io.github.jonloucks.metalog.api.Metalog;
import io.github.jonloucks.metalog.api.Outcome;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.jonloucks.concurrency.api.Idempotent.withClose;
import static io.github.jonloucks.concurrency.api.Idempotent.withOpen;
import static io.github.jonloucks.metalog.impl.Internal.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

final class UnkeyedDispatcherImpl implements Dispatcher, AutoOpen {

    @Override
    public AutoClose open() {
        return withOpen(stateMachine, ()-> this::close);
    }
    
    @Override
    public Outcome dispatch(Meta meta, Runnable work) {
        final Runnable validCommand = commandCheck(work);
        if (stateMachine.getState().isRejecting()) {
            validCommand.run();
            return Outcome.CONSUMED;
        }
        executor.execute(validCommand);
        return Outcome.DISPATCHED;
    }
    
    UnkeyedDispatcherImpl(Metalog.Config config) {
//        this.config = config;
        this.executor = new ThreadPoolExecutor(
            1,
            config.unkeyedThreadCount(),
            60L,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(config.unkeyedFairness())
        );
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        stateMachine = Idempotent.createStateMachine(config.contracts());
        
    }
    
    private void close() {
        withClose(stateMachine, this::realClose);
    }
    
    private void realClose() {
//        final Instant start = Instant.now();
        
        initiateShutdown();
        
        //noinspection LoopStatementThatDoesntLoop
        while (!checkIfShutdown()) {
//            final Duration duration = Duration.between(start, Instant.now());
//            if (isTimeToGiveUp(duration)) {
//                forceShutdown();
                return;
//            }
        }
    }
    
//    private boolean isTimeToGiveUp(Duration duration) {
//        return duration.compareTo(config.shutdownTimeout()) > 0;
//    }
    
    private void initiateShutdown() {
        executor.shutdown();
    }
    
    private boolean checkIfShutdown() {
        final AtomicBoolean shutdown = new AtomicBoolean(false);
        runWithIgnore(() -> shutdown.set(executor.awaitTermination(1, MILLISECONDS)));
        return shutdown.get();
    }
    
//    private void forceShutdown() {
//        executor.shutdownNow();
//    }

    private final StateMachine<Idempotent> stateMachine;
//    private final Metalog.Config config;
    private final ThreadPoolExecutor executor;
}

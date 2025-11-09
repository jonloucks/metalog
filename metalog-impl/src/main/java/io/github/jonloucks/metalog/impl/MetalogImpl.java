package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.concurrency.api.Idempotent;
import io.github.jonloucks.concurrency.api.StateMachine;
import io.github.jonloucks.contracts.api.*;
import io.github.jonloucks.metalog.api.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.github.jonloucks.concurrency.api.Idempotent.withClose;
import static io.github.jonloucks.concurrency.api.Idempotent.withOpen;
import static io.github.jonloucks.contracts.api.Checks.*;
import static io.github.jonloucks.metalog.impl.Internal.*;

final class MetalogImpl implements Metalog {
    
    @Override
    public Outcome publish(Log log, Meta meta) {
        final Log validLog = new InvokeOnlyOnce(log);
        final Meta validMeta = metaCheck(meta);
        
        if (stateMachine.getState().isRejecting()) {
            return Outcome.REJECTED;
        }
        
        if (test(validMeta)) {
            if (shouldTransmitNow(validMeta)) {
                return transmitNow(validLog, validMeta);
            } else {
                return transmitLater(validMeta, validLog);
            }
        } else {
            return Outcome.SKIPPED;
        }
    }
    
    @Override
    public Outcome publish(Log log, Consumer<Meta.Builder<?>> builderConsumer) {
        final Meta.Builder<?> metaBuilder = metaFactory.get();
        builderConsumerCheck(builderConsumer).accept(metaBuilder);
        return publish(logCheck(log), metaBuilder);
    }

    @Override
    public AutoClose addFilter(Predicate<Meta> filter) {
        return filters.addFilter(filter);
    }
    
    @Override
    public boolean test(Meta meta) {
        final Meta validMeta = metaCheck(meta);
        return filters.test(validMeta) && subscribers.stream().anyMatch(matcher(validMeta));
    }
    
    @Override
    public AutoClose subscribe(Subscriber subscriber) {
        final Subscriber validSubscriber = subscriberCheck(subscriber);
        subscribers.add(validSubscriber);
        mainDispatcher.registerSubscriber(validSubscriber);
        return () -> removeSubscriber(validSubscriber);
    }
    
    @Override
    public AutoClose open() {
        return withOpen(stateMachine, this::realOpen);
    }
    
    MetalogImpl(Config config, Repository repository, boolean openRepository) {
        final Repository validRepository = nullCheck(repository, "Repository must be present.");
        this.config = configCheck(config);
        this.closeRepository = openRepository ? validRepository.open() : AutoClose.NONE;
        this.stateMachine = Idempotent.createStateMachine(config.contracts());
    }
    
    private AutoClose realOpen() {
        metaFactory = config.contracts().claim(Meta.Builder.FACTORY);
        this.mainDispatcher = config.contracts().claim(MainDispatcher.CONTRACT);
        activateConsole();
        return this::close;
    }
    
    private Predicate<Subscriber> matcher(Meta meta) {
        if (config.keyedSubscription()) {
            final Optional<String> optionalKey = meta.getKey();
            if (optionalKey.isPresent()) {
                final String key = optionalKey.get();
                return s -> s.getKey().isPresent() && key.equals(s.getKey().get()) && s.test(meta);
            } else {
                return s -> !s.getKey().isPresent() && s.test(meta);
            }
        } else {
            return s -> s.test(meta); // legacy, subscriptions can receive any log
        }
    }

    private void activateConsole() {
        config.contracts().claim(Console.CONTRACT);
    }
    
    private void close() {
        withClose(stateMachine, this::realClose);
    }
    
    private void realClose() {
        closeRepository.close();
    }

    private boolean shouldTransmitNow(Meta meta) {
        return meta.isBlocking();
    }
    
    private Outcome transmitLater(Meta meta, Log log) {
        return forSubscriber(meta, s -> mainDispatcher.dispatch(meta, ()-> guardedDelivery(s, log, meta)));
    }
    
    private Outcome forSubscriber(Meta meta, Function<Subscriber,Outcome> consumer) {
        final AtomicReference<Outcome> outcomeReference = new AtomicReference<>(Outcome.SKIPPED);
        subscribers.stream().filter(matcher(meta)).forEach( subscriber -> {
            final Outcome outcome = consumer.apply(subscriber);
            if (outcome != Outcome.SKIPPED) {
                outcomeReference.set(outcome);
            }
        });
        return outcomeReference.get();
    }
    
    private Outcome transmitNow(Log log, Meta meta) {
        return forSubscriber(meta, s -> guardedDelivery(s, log, meta));
    }
    
    private void removeSubscriber(Subscriber subscriber) {
        if (subscribers.removeIf( x -> x == subscriber)) {
            mainDispatcher.unregisterSubscriber(subscriber);
        }
    }
    
    private final Config config;
    private final StateMachine<Idempotent> stateMachine;
    private final AutoClose closeRepository;
    private final List<Subscriber> subscribers = new CopyOnWriteArrayList<>();
    private final Filterable filters = new FiltersImpl();
    private MainDispatcher mainDispatcher;
    private Supplier<Meta.Builder<?>> metaFactory;
}

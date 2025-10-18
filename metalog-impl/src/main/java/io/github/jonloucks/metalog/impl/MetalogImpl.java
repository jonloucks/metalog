package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.contracts.api.*;
import io.github.jonloucks.metalog.api.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.github.jonloucks.contracts.api.Checks.*;
import static io.github.jonloucks.contracts.api.GlobalContracts.lifeCycle;
import static io.github.jonloucks.metalog.impl.Internal.*;
import static java.lang.ThreadLocal.withInitial;

final class MetalogImpl implements Metalog {
    
    @Override
    public Outcome publish(Log log, Meta meta) {
        final Log validLog = new InvokeOnlyOnce(log);
        final Meta validMeta = metaCheck(meta);
        
        if (idempotent.isRejecting()) {
            return Outcome.REJECTED;
        }
        
        if (test(validMeta)) {
            if (shouldTransmitNow(validMeta)) {
                return transmitNow(validLog, validMeta);
            } else {
                return relayToDispatcher(validMeta, validLog);
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
        return filters.test(meta) && subscribers.stream().anyMatch(s -> s.test(meta));
    }
    
    @Override
    public AutoClose subscribe(Subscriber subscriber) {
        final Subscriber validSubscriber = subscriberCheck(subscriber);
        subscribers.add(validSubscriber);
        return () -> subscribers.removeIf( x -> x == validSubscriber);
    }

    @Override
    public AutoClose open() {
        return idempotent.transitionToOpened(this::realOpen);
    }
    
    MetalogImpl(Config config, Repository repository, boolean openRepository) {
        this.config = configCheck(config);
        this.repository = nullCheck(repository, "Repository must be present.");
        this.closeRepository = openRepository ? repository.open() : AutoClose.NONE;
        this.idempotent = config.contracts().claim(Idempotent.FACTORY).get();
    }
    
    private AutoClose realOpen() {
        metaFactory = config.contracts().claim(Meta.Builder.FACTORY);
        keyedDispatcherFactory = config.contracts().claim(Dispatcher.KEYED_FACTORY);
        unkeyedDispatcherFactory = config.contracts().claim(Dispatcher.UNKEYED_FACTORY);
        createUnkeyedDispatcher();
        activateConsole();
        return this::close;
    }

    private void createUnkeyedDispatcher() {
        final Contracts contracts = config.contracts();
        final Contract<Dispatcher> contract = Contract.create(Dispatcher.class, n -> n.name("Unkeyed Dispatcher"));
        repository.keep(contract, lifeCycle(unkeyedDispatcherFactory::get));
        dispatchers.put(UNKEYED, contracts.claim(contract));
    }
    
    private void activateConsole() {
        config.contracts().claim(Console.CONTRACT);
    }
    
    private void close() {
        idempotent.transitionToClosed(this::realClose);
    }
    
    private void realClose() {
        closeRepository.close();
    }
    
    private boolean shouldTransmitNow(Meta meta) {
        return meta.isBlocking() || onDispatchingThread();
    }
    
    private Outcome relayToDispatcher(Meta meta, Log log) {
        final Dispatcher dispatcher = chooseDispatcher(meta);
        final AtomicReference<Outcome> outcomeReference = new AtomicReference<>(Outcome.SKIPPED);
        
        subscribers.forEach(subscriber -> {
            if (subscriber.test(meta)) {
                outcomeReference.set(relayWithContext(subscriber, dispatcher, meta, log));
            }
        });
        
        return outcomeReference.get();
    }
    
    private Outcome relayWithContext(Subscriber subscriber, Dispatcher dispatcher, Meta meta, Log log) {
        return dispatcher.dispatch(meta,  () -> {
            final Map<String,Object> context = THREAD_CONTEXT.get();
            final Object oldValue = context.put(DISPATCHING_PROPERTY, true);
            try {
                subscriber.receive(log, meta);
            } finally {
                context.put(DISPATCHING_PROPERTY, oldValue);
            }
        });
    }
    
    private boolean onDispatchingThread() {
        return Boolean.TRUE.equals(THREAD_CONTEXT.get().get(DISPATCHING_PROPERTY));
    }
    
    private Dispatcher chooseDispatcher(Meta meta) {
        final String key = meta.getKey().orElse("");
        return dispatchers.computeIfAbsent(key, this::createKeyedDispatcher);
    }
    
    private Dispatcher createKeyedDispatcher(String key) {
        final Contract<Dispatcher> contract = Contract.create(Dispatcher.class, n -> n.name("Keyed Dispatcher " + key));
        repository.keep(contract, lifeCycle(keyedDispatcherFactory::get));
        return config.contracts().claim(contract);
    }
    
    private Outcome transmitNow(Log log, Meta meta) {
        final AtomicReference<Outcome> outcomeReference = new AtomicReference<>(Outcome.SKIPPED);
        subscribers.forEach(subscriber -> {
            if (subscriber.test(meta)) {
                outcomeReference.set(subscriber.receive(log, meta));
            }
        });
        return outcomeReference.get();
    }
    
    private static final ThreadLocal<Map<String, Object>> THREAD_CONTEXT = withInitial(LinkedHashMap::new);
    private static final String DISPATCHING_PROPERTY = "dispatching";
    private static final String UNKEYED = "";
    
    private final Config config;
    private final Idempotent idempotent;
    private final Repository repository;
    private final AutoClose closeRepository;
    private final List<Subscriber> subscribers = new CopyOnWriteArrayList<>();
    private final Filterable filters = new FiltersImpl();
    private final ConcurrentHashMap<String,Dispatcher> dispatchers = new ConcurrentHashMap<>();
    private Supplier<Meta.Builder<?>> metaFactory;
    private Supplier<Dispatcher> keyedDispatcherFactory;
    private Supplier<Dispatcher> unkeyedDispatcherFactory;
}

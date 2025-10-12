package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.contracts.api.*;
import io.github.jonloucks.metalog.api.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.github.jonloucks.contracts.api.Checks.*;
import static io.github.jonloucks.metalog.impl.Internal.*;
import static java.lang.ThreadLocal.withInitial;
import static java.util.Optional.ofNullable;

final class MetalogImpl implements Metalog {
    
    @Override
    public void publish(Log log, Meta meta) {
        final Log validLog = new InvokeOnlyOnce(log);
        final Meta validMeta = null == meta ? Meta.DEFAULT : meta;
        
        if (idempotent.isRejecting()) {
            return;
        }
        
        if (shouldProcess(validMeta)) {
            if (shouldTransmitNow(validMeta)) {
                transmitNow(validLog, validMeta);
            } else {
                relayToDispatcher(validMeta, validLog);
            }
        }
    }
    
    @Override
    public void publish(Log log, Consumer<Meta.Builder<?>> builderConsumer) {
        final Meta.Builder<?> metaBuilder = metaFactory.get();
        builderConsumerCheck(builderConsumer).accept(metaBuilder);
        publish(logCheck(log), metaBuilder);
    }

    @Override
    public AutoClose addFilter(Predicate<Meta> filter) {
        return filters.addFilter(filter);
    }
    
    @Override
    public boolean test(Meta meta) {
        return filters.test(meta);
    }
    
    @Override
    public AutoClose subscribe(Subscriber subscriber) {
        final Subscriber validSubscriber = subscriberCheck(subscriber);
        subscribers.add(validSubscriber);
        return () -> subscribers.removeIf( x -> x == validSubscriber);
    }

    @Override
    public AutoClose open() {
        if (idempotent.transitionToOpening()) {
            return realOpening();
        } else {
            return () -> {}; // all open calls after the first get a do nothing close
        }
    }
    
    MetalogImpl(Config config) {
        this.config = configCheck(config);
        this.repository = config.contracts().claim(Repository.FACTORY).get();
        
        bindContracts(config);
    }
    
    private AutoClose realOpening() {
        try {
            realOpen();
            idempotent.transitionToOpened();
        } catch (Exception thrown) {
            if (idempotent.transitionToFailed()) {
                throw thrown;
            }
        }
        return this::close;
    }
    private void realOpen() {
        closeRepository = repository.open();
        metaFactory = config.contracts().claim(Meta.Builder.FACTORY_CONTRACT);
        createUnkeyedDispatcher();
        activateConsole();
    }
    
    private void createUnkeyedDispatcher() {
        final Promisors promisors = config.contracts().claim(Promisors.CONTRACT);
        final Contract<Dispatcher> contract = Contract.create(Dispatcher.class, n -> n.name("Unkeyed Dispatcher"));
        repository.keep(contract, promisors.createLifeCyclePromisor(()-> new UnkeyedDispatcherImpl(config)));
        dispatchers.put("", config.contracts().claim(contract));
    }
    
    private void activateConsole() {
        if (config.systemOutput() && config.contracts().isBound(Console.CONTRACT)) {
            config.contracts().claim(Console.CONTRACT);
        }
    }
    
    private void close() {
        if (idempotent.transitionToClosed()) {
            realClose();
        }
    }
    
    private void realClose() {
        ofNullable(closeRepository).ifPresent(close -> {
            repository = null;
            closeRepository = null;
            close.close();
        });
    }
    
    private boolean shouldTransmitNow(Meta meta) {
        return meta.hasBlock() || onDispatchingThread();
    }
    
    private boolean shouldProcess(Meta meta) {
        if (!subscribers.isEmpty() && test(meta)) {
            return subscribers.stream().anyMatch(s -> s.test(meta));
        }
        return false;
    }
    
    private void relayToDispatcher(Meta meta, Log log) {
        final Dispatcher dispatcher = chooseDispatcher(meta);
        subscribers.forEach(subscriber -> {
            if (subscriber.test(meta)) {
                relayWithContext(subscriber, dispatcher, meta, log);
            }
        });
    }
    
    private void relayWithContext(Subscriber subscriber, Dispatcher dispatcher, Meta meta, Log log) {
        dispatcher.dispatch(meta,  () -> {
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
        final Promisors promisors = config.contracts().claim(Promisors.CONTRACT);
        final Contract<Dispatcher> contract = Contract.create(Dispatcher.class, n -> n.name("Keyed Dispatcher " + key));
        repository.keep(contract, promisors.createLifeCyclePromisor(()-> new KeyedDispatcherImpl(config)));
        return config.contracts().claim(contract);
    }
    
    private void bindContracts(Config config) {
        final Promisors promisors = config.contracts().claim(Promisors.CONTRACT);
        repository.keep(Metalog.CONTRACT, () -> this);
        repository.keep(MetalogFactory.CONTRACT, promisors.createLifeCyclePromisor(MetalogFactoryImpl::new));
        repository.keep(Entities.Builder.FACTORY_CONTRACT, () -> EntitiesImpl::new);
        repository.keep(Entity.Builder.FACTORY_CONTRACT, () -> EntityImpl::new);
        repository.keep(Meta.Builder.FACTORY_CONTRACT, () -> MetaImpl::new);
        repository.keep(Console.CONTRACT, promisors.createLifeCyclePromisor(() -> new ConsoleImpl(config)));
    }
    
    private void transmitNow(Log log, Meta meta) {
        subscribers.forEach(subscriber -> {
            if (subscriber.test(meta)) {
                subscriber.receive(log, meta);
            }
        });
    }
    
    private static final ThreadLocal<Map<String, Object>> THREAD_CONTEXT = withInitial(LinkedHashMap::new);
    private static final String DISPATCHING_PROPERTY = "dispatching";
    
    private final Config config;
    private final IdempotentImpl idempotent = new IdempotentImpl();
    private Repository repository;
    private AutoClose closeRepository;
    private final List<Subscriber> subscribers = new CopyOnWriteArrayList<>();
    private final Filterable filters = new FiltersImpl();
    private final ConcurrentHashMap<String,Dispatcher> dispatchers = new ConcurrentHashMap<>();
    private Supplier<Meta.Builder<?>> metaFactory;
}

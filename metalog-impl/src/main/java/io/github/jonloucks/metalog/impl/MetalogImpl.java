package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.contracts.api.*;
import io.github.jonloucks.metalog.api.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.github.jonloucks.contracts.api.Checks.*;
import static io.github.jonloucks.metalog.impl.Internal.*;
import static java.util.Optional.ofNullable;

final class MetalogImpl implements Metalog {
    
    @Override
    public void publish(Log log, Meta meta) {
        if (openState.isRejecting()) {
            throw new IllegalStateException("Metalog must be open.");
        }
        
        final Log validLog = new InvokeOnlyOnce(log);
        final Meta validMeta = null == meta ? Meta.DEFAULT : meta;
        
        if (!subscribers.isEmpty() && test(validMeta)) {
            if (validMeta.isBlock()) {
                transmitToAll(validLog, validMeta);
            } else {
                final Dispatcher dispatcher = chooseDispatcher(validMeta);
                subscribers.forEach(subscriber -> {
                    if (subscriber.test(validMeta)) {
                        dispatcher.dispatch(validMeta, () -> transmitToSubscriber(subscriber, validLog, validMeta));
                    }
                });
            }
        }
    }
    
    private Dispatcher chooseDispatcher(Meta meta) {
        final String key = meta.getKey().orElse("");
        return dispatchers.computeIfAbsent(key, this::createKeyedDispatcher);
    }
    
    private Dispatcher createKeyedDispatcher(String key) {
        final Promisors promisors = config.contracts().claim(Promisors.CONTRACT);
        final Contract<Dispatcher> contract = Contract.create(Dispatcher.class, n -> n.name("Keyed Dispatcher " + key));
        repository.store(contract, promisors.createLifeCyclePromisor(()-> new KeyedDispatcherImpl(config)));
        return config.contracts().claim(contract);
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
        final Subscriber validSubscriber = nullCheck(subscriber, "Subscriber must be present.");
        subscribers.add(validSubscriber);
        return () -> subscribers.removeIf( x -> x == validSubscriber);
    }

    @Override
    public AutoClose open() {
        if (openState.transitionToOpened()) {
            realOpen();
            return this::close;
        } else {
            return () -> {}; // all open calls after the first get a do nothing close
        }
    }
    
    MetalogImpl(Config config) {
        this.config = configCheck(config);
        final Promisors promisors = config.contracts().claim(Promisors.CONTRACT);
        this.repository = config.contracts().claim(Repository.FACTORY).get();
        
        bindContracts(config, promisors);
    }
    
    private void realOpen() {
        final Promisors promisors = config.contracts().claim(Promisors.CONTRACT);
        closeRepository = repository.open();
//        dispatchers.put("",config.contracts().claim(Dispatcher.KEYED_CONTRACT));
        
        final Contract<Dispatcher> unkeyedContract = Contract.create(Dispatcher.class, n -> n.name("Unkeyed Dispatcher"));
        repository.store(unkeyedContract, promisors.createLifeCyclePromisor(()-> new UnkeyedDispatcherImpl(config)));
        dispatchers.put("", config.contracts().claim(unkeyedContract));
//            promisors.createLifeCyclePromisor(()-> new UnkeyedDispatcherImpl(config)
//        unkeyedDispatcher = config.contracts().claim(Dispatcher.UNKEYED_CONTRACT);
        
        metaFactory = config.contracts().claim(Meta.Builder.FACTORY_CONTRACT);
        if (config.systemOutput()) {
            config.contracts().claim(Console.CONTRACT);
        }
    }
    
    private void close() {
        if (openState.transitionToClosed()) {
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
    
    @SuppressWarnings("resource") // keeping promises alive for life of Metalog
    private void bindContracts(Config config, Promisors promisors) {
        repository.store(Metalog.CONTRACT, () -> this);
        repository.store(MetalogFactory.CONTRACT, promisors.createLifeCyclePromisor(MetalogFactoryImpl::new));
        repository.store(Entities.Builder.FACTORY_CONTRACT, () -> EntitiesImpl::new);
        repository.store(Entity.Builder.FACTORY_CONTRACT, () -> EntityImpl::new);
        repository.store(Meta.Builder.FACTORY_CONTRACT, () -> MetaImpl::new);
        repository.store(Console.CONTRACT, promisors.createLifeCyclePromisor(() -> new ConsoleImpl(config)));
    }
    
    private void transmitToAll(Log log, Meta meta) {
        subscribers.forEach(subscriber -> {
            if (subscriber.test(meta)) {
                transmitToSubscriber(subscriber, log, meta);
            }
        });
    }
    
    private void transmitToSubscriber(Subscriber subscriber, Log log, Meta meta) {
        final Instant start = Instant.now();
        try {
            subscriber.receive(log, meta);
        } catch (Throwable ignored) {
        } finally {
            final Duration duration = Duration.between(start, Instant.now());
            if (duration.compareTo(Duration.ofSeconds(1)) > 0) {
                // slow
            }
        }
    }
    
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final Config config;
    private final IdempotentImpl openState = new IdempotentImpl();
    private Repository repository;
    private AutoClose closeRepository;
    private final List<Subscriber> subscribers = new CopyOnWriteArrayList<>();
    private final Filterable filters = new FiltersImpl();
    private final ConcurrentHashMap<String,Dispatcher> dispatchers = new ConcurrentHashMap<>();
    private Supplier<Meta.Builder<?>> metaFactory;
}

package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.contracts.api.*;
import io.github.jonloucks.metalog.api.*;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.github.jonloucks.contracts.api.Checks.*;
import static io.github.jonloucks.metalog.impl.Internal.*;
import static java.util.Optional.ofNullable;

final class MetalogImpl implements Metalog, AutoClose {
    
    @Override
    public void publish(Log log, Meta meta) {
        if (!openState.isOpen()) {
            throw new IllegalStateException("Metalog must be open.");
        }
        
        final Log validLog = new InvokeOnlyOnce(log);
        final Meta validMeta = null == meta ? Meta.DEFAULT : meta;
        
        if (!subscribers.isEmpty() && test(validMeta)) {
            if (validMeta.isBlock()) {
                dispatch(validLog, validMeta);
            } else {
                dispatcher.execute(() -> dispatch(validLog, validMeta));
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
        final Subscriber validSubscriber = nullCheck(subscriber, "Subscriber must be present.");
        subscribers.add(validSubscriber);
        return () -> subscribers.removeIf( x -> x == validSubscriber);
    }

    @Override
    public AutoClose open() {
        if (openState.transitionToOpened()) {
            realOpen();
            return this;
        } else {
            return () -> {}; // all open calls after the first get a do nothing close
        }
    }

    @Override
    public void close() {
        if (openState.transitionToClosed()) {
            realClose();
        }
    }
    
    MetalogImpl(Config config) {
        this.config = configCheck(config);
        final Promisors promisors = config.contracts().claim(Promisors.CONTRACT);
        this.repository = config.contracts().claim(Repository.FACTORY).get();
        
        bindContracts(config, promisors);
    }
    
    private void realOpen() {
        ofNullable(repository).ifPresent(Repository::open);
        dispatcher = config.contracts().claim(Dispatcher.CONTRACT);
        metaFactory = config.contracts().claim(Meta.Builder.FACTORY_CONTRACT);
        if (config.systemOutput()) {
            config.contracts().claim(Console.CONTRACT);
        }
    }
    
    private void realClose() {
        ofNullable(repository).ifPresent(close -> {
            repository = null;
            close.close();
        });
    }
    
    private void bindContracts(Config config, Promisors promisors) {
        repository.store(Metalog.CONTRACT, () -> this);
        repository.store(MetalogFactory.CONTRACT, promisors.createLifeCyclePromisor(MetalogFactoryImpl::new));
        repository.store(Dispatcher.CONTRACT, promisors.createLifeCyclePromisor(()-> new DispatcherImpl(config)));
        repository.store(Entities.Builder.FACTORY_CONTRACT, () -> EntitiesImpl::new);
        repository.store(Entity.Builder.FACTORY_CONTRACT, () -> EntityImpl::new);
        repository.store(Meta.Builder.FACTORY_CONTRACT, () -> MetaImpl::new);
        repository.store(Console.CONTRACT, promisors.createLifeCyclePromisor(() -> new ConsoleImpl(config)));
    }
    
    private void dispatch(Log log, Meta meta) {
        subscribers.forEach(s -> s.receive(log, meta));
    }
    
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final Config config;
    private final IdempotentImpl openState = new IdempotentImpl();
    private Repository repository;
    private final List<Subscriber> subscribers = new CopyOnWriteArrayList<>();
    private final Filterable filters = new FiltersImpl();
    private Dispatcher dispatcher;
    private Supplier<Meta.Builder<?>> metaFactory;
}

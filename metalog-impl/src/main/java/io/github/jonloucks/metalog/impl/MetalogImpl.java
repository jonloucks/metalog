package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.contracts.api.*;
import io.github.jonloucks.metalog.api.*;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.github.jonloucks.contracts.api.Checks.nullCheck;
import static io.github.jonloucks.metalog.impl.Internal.*;
import static java.util.Optional.ofNullable;

final class MetalogImpl implements Metalog, AutoClose {
    
    @Override
    public void publish(Log log, Meta meta) {
        final Log validLog = logCheck(log);
        final Meta validMeta = metaCheck(meta);
        
        if (!openState.isOpen()) {
            throw new IllegalStateException("Metalog is not open");
        }
        
        if (!subscribers.isEmpty() && isEnabled() && test(validMeta)) {
            if (validMeta.isBlocking()) {
                dispatch(validLog, validMeta);
            } else {
                dispatcher.execute(() -> dispatch(log, meta));
            }
        }
    }
  
    @Override
    public void publish(Log log, Consumer<Meta.Builder<?>> builderConsumer) {
        final Log validLog = logCheck(log);
        final Consumer<Meta.Builder<?>> validBuilderConsumer = nullCheck(builderConsumer, "builderConsumer was null");
        
        final Meta.Builder<?> metaBuilder = metaFactory.get();
        
        validBuilderConsumer.accept(metaBuilder);
        
        publish(validLog, metaBuilder);
    }
 
    @Override
    public boolean isEnabled() {
        return config.isEnabled();
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
    public AutoClose subscribe(Subscriber config) {
        final Subscriber validSubscriber = nullCheck(config, "subscriber was null");

        subscribers.add(validSubscriber);
        return () -> subscribers.removeIf( x -> x == validSubscriber);
    }

    @Override
    public AutoClose open() {
        if (openState.transitionToOpen()) {
            ofNullable(repository).ifPresent(Repository::open);
            dispatcher = config.contracts().claim(Dispatcher.CONTRACT);
            metaFactory = config.contracts().claim(Meta.Builder.FACTORY_CONTRACT);
            if (config.systemOutput()) {
                config.contracts().claim(SystemSubscriber.CONTRACT);
            }
            return this;
        }
        return () -> {};
    }
    
    @Override
    public void close() {
        if (openState.transitionToClosed()) {
            ofNullable(repository).ifPresent(Repository::close);
            repository = null;
        }
    }
    
    MetalogImpl(Config config) {
        this.config = configCheck(config);
        final Promisors promisors = config.contracts().claim(Promisors.CONTRACT);
        this.repository = config.contracts().claim(Repository.FACTORY).get();
        
        bindContracts(config, promisors);
    }
    
    private void bindContracts(Config config, Promisors promisors) {
        repository.store(Metalog.CONTRACT, () -> this);
        repository.store(MetalogFactory.CONTRACT, promisors.createLifeCyclePromisor(MetalogFactoryImpl::new));
        repository.store(Dispatcher.CONTRACT, promisors.createLifeCyclePromisor(()-> new DispatcherImpl(config)));
        repository.store(Entities.Builder.FACTORY_CONTRACT, () -> EntitiesImpl::new);
        repository.store(Entity.Builder.FACTORY_CONTRACT, () -> EntityImpl::new);
        repository.store(Meta.Builder.FACTORY_CONTRACT, () -> MetaImpl::new);
        repository.store(SystemSubscriber.CONTRACT, promisors.createLifeCyclePromisor(() -> new SystemSubscriberImpl(config)));
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

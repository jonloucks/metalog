package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.AutoOpen;
import io.github.jonloucks.metalog.api.*;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.github.jonloucks.contracts.api.Checks.builderConsumerCheck;
import static io.github.jonloucks.metalog.impl.Internal.logCheck;
import static java.util.Optional.ofNullable;

final class ConsoleImpl implements Console, AutoOpen {

    @Override
    public void publish(Log log, Meta meta) {
        receive(log, meta);
    }
    
    @Override
    public void publish(Log log, Consumer<Meta.Builder<?>> builderConsumer) {
        final Meta.Builder<?> metaBuilder = metaFactory.get();
        
        builderConsumerCheck(builderConsumer).accept(metaBuilder);
        
        publish(logCheck(log), metaBuilder);
    }
    
    @Override
    public void receive(Log log, Meta meta) {
        if (test(meta)) {
            switch (meta.getChannel()) {
                case "System.out":
                case "Console":
                    System.out.println(log.get());
                    break;
                case "System.err":
                    System.err.println(log.get());
                    break;
            }
        }
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
    public AutoClose open() {
        if (openState.transitionToOpened()) {
            metaFactory = config.contracts().claim(Meta.Builder.FACTORY_CONTRACT);
            closeSubscription = config.contracts().claim(Metalog.CONTRACT).subscribe(this);
            return this::close;
        } else {
            return () -> {}; // all open calls after the first get a do nothing close
        }
    }
    
    ConsoleImpl(Metalog.Config config) {
        this.config = config;
    }
    
    private void close() {
        if (openState.transitionToClosed()) {
            ofNullable(closeSubscription).ifPresent(close -> {
                closeSubscription = null;
                close.close();
            });
        }
    }
    
    private final Filterable filters = new FiltersImpl();
    private final IdempotentImpl openState = new IdempotentImpl();
    private final Metalog.Config config;
    private Supplier<Meta.Builder<?>> metaFactory;
    private AutoClose closeSubscription;
}

package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.AutoOpen;
import io.github.jonloucks.metalog.api.*;

import java.util.function.Predicate;

final class SystemSubscriberImpl implements SystemSubscriber, Filterable, AutoOpen, AutoClose {
    
    private final Metalog.Config config;
    
    @Override
    public void receive(Log log, Meta meta) {
        if (test(meta)) {
            switch (meta.getChannel()) {
                case "info":
                case "system.out":
                    System.out.println(log.get());
                    break;
                case "error":
                case "warn":
                case "system.err":
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
        if (openState.transitionToOpen()) {
            closeSubscription = config.contracts().claim(Metalog.CONTRACT).subscribe(this);
        }
        return () -> {};
    }
    
    @Override
    public void close() {
        if (openState.transitionToClosed()) {
            final AutoClose autoClose = closeSubscription;
            closeSubscription = null;
            if ( autoClose != null) {
                autoClose.close();
            }
        }
    }
    
    SystemSubscriberImpl(Metalog.Config config) {
        this.config = config;
    }
    
    private final IdempotentImpl openState = new IdempotentImpl();
    private final Filterable filters = new FiltersImpl();
    private AutoClose closeSubscription;
}

package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.Contract;
import io.github.jonloucks.contracts.api.Repository;
import io.github.jonloucks.metalog.api.*;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static io.github.jonloucks.contracts.api.GlobalContracts.lifeCycle;
import static io.github.jonloucks.metalog.impl.Internal.*;
import static java.util.Optional.ofNullable;

final class MainDispatcherImpl implements MainDispatcher {

    @Override
    public Outcome dispatch(Meta meta, Runnable job) {
        final Optional<Dispatcher> foundDispatcher = findDispatcher(meta);
        if (foundDispatcher.isPresent()) {
            return foundDispatcher.get().dispatch(meta, job);
        }
        return Outcome.SKIPPED;
    }
    
    @Override
    public AutoClose open() {
        return AutoClose.NONE;
    }
    
    @Override
    public void registerSubscriber(Subscriber subscriber) {
        if (config.keyedSubscription()) {
            final Subscriber validSubscriber = subscriberCheck(subscriber);
            final String key = validSubscriber.getKey().orElse("");
            incrementKeyUsage(key);
        }
    }
    
    @Override
    public void unregisterSubscriber(Subscriber subscriber) {
        if (config.keyedSubscription()) {
            final Subscriber validSubscriber = subscriberCheck(subscriber);
            final String key = validSubscriber.getKey().orElse("");
            decrementKeyUsage(key);
        }
    }
    
    MainDispatcherImpl(Metalog.Config config, Repository repository) {
        this.config = config;
        this.repository = repository;
    }
    
    private Optional<Dispatcher> findDispatcher(Meta meta) {
        final Meta validMeta = metaCheck(meta);
        final String key = validMeta.getKey().orElse("");
        if (config.keyedSubscription()) {
            return ofNullable(keySmartMap.get(key)).flatMap(Smart::get);
        } else {
            return keySmartMap.computeIfAbsent(key, k -> {
                final Smart smart = new Smart(k);
                smart.incrementUsage();
                return smart;
            }).get();
        }
    }
    
    private void incrementKeyUsage(String key) {
        keySmartMap.computeIfAbsent(keyCheck(key), Smart::new).incrementUsage();
    }
    
    private void decrementKeyUsage(String key) {
        ofNullable(keySmartMap.get(keyCheck(key))).ifPresent(Smart::decrementUsage);
    }
    
    private Supplier<Dispatcher> getDispatcherFactory(String key) {
        return config.contracts().claim(key.isEmpty() ? UNKEYED_FACTORY : KEYED_FACTORY);
    }
    
    private final class Smart {
        Smart(String key) {
            this.key = key;
            this.contract = Contract.create(Dispatcher.class, n -> n.name("Dispatcher " + key));
        }
        
        Optional<Dispatcher> get() {
            final Optional<Dispatcher> optionalDispatcher = ofNullable(dispatcher);
            if (optionalDispatcher.isPresent()) {
                return optionalDispatcher;
            }
            return acquire();
        }
        
        private synchronized Optional<Dispatcher> acquire() {
            final Optional<Dispatcher> optionalDispatcher = ofNullable(dispatcher);
            if (optionalDispatcher.isPresent()) {
                return optionalDispatcher;
            }
            closeBinding = repository.store(contract, lifeCycle(getDispatcherFactory(key)::get));
            dispatcher = config.contracts().claim(contract);
            
            return Optional.of(dispatcher);
        }
        
        synchronized void incrementUsage() {
            ++usageCount;
        }
        
        synchronized void decrementUsage() {
            if (--usageCount == 0) {
                ofNullable(closeBinding).ifPresent(AutoClose::close);
                keySmartMap.remove(key, this);
            }
        }
        
        private final String key;
        private final Contract<Dispatcher> contract;
        private volatile Dispatcher dispatcher;
        private int usageCount = 0;
        private AutoClose closeBinding;
    }
    
    private final Metalog.Config config;
    private final Repository repository;
    private final ConcurrentHashMap<String, Smart> keySmartMap = new ConcurrentHashMap<>();
}

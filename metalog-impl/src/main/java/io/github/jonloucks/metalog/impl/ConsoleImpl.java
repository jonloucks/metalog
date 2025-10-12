package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.AutoOpen;
import io.github.jonloucks.metalog.api.*;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.github.jonloucks.contracts.api.Checks.builderConsumerCheck;
import static io.github.jonloucks.metalog.impl.Internal.logCheck;
import static io.github.jonloucks.metalog.impl.Internal.metaCheck;
import static java.util.Optional.ofNullable;

final class ConsoleImpl implements Console, AutoOpen {
    
    @Override
    public void info(Log log) {
        publish(log, INFO_META);
    }
    
    @Override
    public void error(Log log) {
        publish(log, ERROR_META);
    }
    
    @Override
    public void publish(Log log) {
        publish(log, INFO_META);
    }

    @Override
    public void publish(Log log, Meta meta) {
        if (test(meta)) {
            metalog.publish(log, meta);
        }
    }

    @Override
    public void publish(Log log, Consumer<Meta.Builder<?>> builderConsumer) {
        metalog.publish(log, b -> {
            b.key(CONSOLE_KEY).channel(CONSOLE_INFO_CHANNEL);
            builderConsumerCheck(builderConsumer).accept(b);
        });
    }
    
    @Override
    public void receive(Log log, Meta meta) {
        final Log validLog = logCheck(log);
        final Meta validMeta = metaCheck(meta);
        if (test(validMeta)) {
            switch (meta.getChannel()) {
                case CONSOLE_INFO_CHANNEL:
                case SYSTEM_INFO_CHANNEL:
                    System.out.println(validLog.get());
                    break;
                case SYSTEM_ERROR_CHANNEL:
                case CONSOLE_ERROR_CHANNEL:
                    System.err.println(validLog.get());
                    break;
                default:
            }
        }
    }
  
    @Override
    public AutoClose addFilter(Predicate<Meta> filter) {
        return filters.addFilter(filter);
    }
    
    @Override
    public boolean test(Meta meta) {
        return isSupported(metaCheck(meta)) && filters.test(meta);
    }
    
    @Override
    public AutoClose open() {
        if (openState.transitionToOpened()) {
            return realOpen();
        } else {
            return () -> {}; // all open calls after the first get a do nothing close
        }
    }
    
    ConsoleImpl(Metalog.Config config) {
        this.config = config;
    }

    private AutoClose realOpen() {
        metalog = config.contracts().claim(Metalog.CONTRACT);
        closeSubscription = config.contracts().claim(Metalog.CONTRACT).subscribe(this);
        return this::close;
    }
    
    private void close() {
        if (openState.transitionToClosed()) {
            ofNullable(closeSubscription).ifPresent(close -> {
                closeSubscription = null;
                close.close();
            });
        }
    }
    
    private boolean isSupported(Meta meta) {
        switch (meta.getChannel()) {
            case SYSTEM_INFO_CHANNEL:
            case SYSTEM_ERROR_CHANNEL:
            case CONSOLE_ERROR_CHANNEL:
            case CONSOLE_INFO_CHANNEL:
                return true;
            default:
                return false;
        }
    }
    
    private static Meta makeMeta(String channel) {
        return new MetaImpl().key(CONSOLE_KEY).channel(channel);
    }
    
    private static final String CONSOLE_KEY = "Console";
    private static final String CONSOLE_INFO_CHANNEL = "Console.info";
    private static final String CONSOLE_ERROR_CHANNEL = "Console.error";
    private static final String SYSTEM_ERROR_CHANNEL = "System.err";
    private static final String SYSTEM_INFO_CHANNEL = "System.out";
    private static final Meta ERROR_META = makeMeta(CONSOLE_ERROR_CHANNEL);
    private static final Meta INFO_META = makeMeta(CONSOLE_INFO_CHANNEL);
    
    private final Filterable filters = new FiltersImpl();
    private final IdempotentImpl openState = new IdempotentImpl();
    private final Metalog.Config config;
    private Metalog metalog;
    private AutoClose closeSubscription;
}

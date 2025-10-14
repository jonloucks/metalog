package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.AutoOpen;
import io.github.jonloucks.metalog.api.*;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.github.jonloucks.contracts.api.Checks.builderConsumerCheck;
import static io.github.jonloucks.metalog.impl.Internal.logCheck;
import static io.github.jonloucks.metalog.impl.Internal.metaCheck;
import static java.util.Optional.ofNullable;

final class ConsoleImpl implements Console, AutoOpen {
    
    @Override
    public Outcome output(Log log) {
        return publish(log, OUTPUT_META);
    }
    
    @Override
    public Outcome error(Log log) {
        return publish(log, ERROR_META);
    }
    
    @Override
    public Outcome publish(Log log) {
        return publish(log, OUTPUT_META);
    }

    @Override
    public Outcome publish(Log log, Meta meta) {
        if (test(meta)) {
            final Outcome outcome = metalog.publish(log, meta);
            if (outcome == Outcome.REJECTED) {
                // directly process message
                return receive(log,meta);
            }
            return outcome;
        } else {
            return Outcome.SKIPPED;
        }
    }

    @Override
    public Outcome publish(Log log, Consumer<Meta.Builder<?>> builderConsumer) {
        final Meta.Builder<?> metaBuilder = metaFactory.get();
        metaBuilder.channel(CONSOLE_OUTPUT_CHANNEL);
        builderConsumerCheck(builderConsumer).accept(metaBuilder);
        metaBuilder.key(CONSOLE_KEY);
        return publish(logCheck(log), metaBuilder);
    }
    
    @Override
    public Outcome receive(Log log, Meta meta) {
        final Log validLog = logCheck(log);
        final Meta validMeta = metaCheck(meta);
        if (test(validMeta)) {
            return getPrintStream(validMeta).map(toPrint(validLog)).orElse(Outcome.SKIPPED);
        }
        return Outcome.SKIPPED;
    }
    
    private static Function<PrintStream, Outcome> toPrint(Log log) {
        return printStream -> {
            printStream.println(log.get());
            return Outcome.CONSUMED;
        };
    }
    
    private Optional<PrintStream> getPrintStream(Meta meta) {
        return ofNullable(printMap.get(meta.getChannel()));
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
        return idempotent.transitionToOpened(this::realOpen);
    }
    
    ConsoleImpl(Metalog.Config config) {
        this.config = config;
        printMap.put(SYSTEM_OUT_CHANNEL, System.out);
        printMap.put(SYSTEM_ERR_CHANNEL, System.err);
        printMap.put(CONSOLE_OUTPUT_CHANNEL, System.out);
        printMap.put(CONSOLE_ERROR_CHANNEL, System.err);
    }

    private AutoClose realOpen() {
        metalog = config.contracts().claim(Metalog.CONTRACT);
        metaFactory = config.contracts().claim(Meta.Builder.FACTORY_CONTRACT);
        closeSubscription = config.contracts().claim(Metalog.CONTRACT).subscribe(this);
        return this::close;
    }
    
    private void close() {
        idempotent.transitionToClosed(this::realClose);
    }
    
    private void realClose() {
        ofNullable(closeSubscription).ifPresent(close -> {
            closeSubscription = null;
            close.close();
        });
    }
    
    private boolean isSupported(Meta meta) {
        return printMap.containsKey(meta.getChannel());
    }
    
    private static Meta makeMeta(String channel) {
        return new MetaImpl().key(CONSOLE_KEY).channel(channel);
    }
    
    private static final String CONSOLE_KEY = "Console";
    private static final String CONSOLE_OUTPUT_CHANNEL = "Console.output";
    private static final String CONSOLE_ERROR_CHANNEL = "Console.error";
    private static final String SYSTEM_ERR_CHANNEL = "System.err";
    private static final String SYSTEM_OUT_CHANNEL = "System.out";
    private static final Meta ERROR_META = makeMeta(CONSOLE_ERROR_CHANNEL);
    private static final Meta OUTPUT_META = makeMeta(CONSOLE_OUTPUT_CHANNEL);
    
    private final Map<String, PrintStream> printMap = new HashMap<>();
    private final Filterable filters = new FiltersImpl();
    private final IdempotentImpl idempotent = new IdempotentImpl();
    private final Metalog.Config config;
    private Metalog metalog;
    private AutoClose closeSubscription;
    private Supplier<Meta.Builder<?>> metaFactory;
}

package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.concurrency.api.Idempotent;
import io.github.jonloucks.concurrency.api.StateMachine;
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

import static io.github.jonloucks.concurrency.api.Idempotent.withClose;
import static io.github.jonloucks.concurrency.api.Idempotent.withOpen;
import static io.github.jonloucks.contracts.api.Checks.builderConsumerCheck;
import static io.github.jonloucks.contracts.api.Checks.configCheck;
import static io.github.jonloucks.metalog.impl.Internal.logCheck;
import static io.github.jonloucks.metalog.impl.Internal.metaCheck;
import static java.util.Optional.ofNullable;

final class ConsoleImpl implements Console, AutoOpen {
    
    @Override
    public Outcome output(Log log) {
        return publish(log, outputMeta);
    }
    
    @Override
    public Outcome error(Log log) {
        return publish(log, errorMeta);
    }
    
    @Override
    public Outcome publish(Log log) {
        return publish(log, outputMeta);
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
        return ofNullable(channelPrintStreamMap.get(meta.getChannel()));
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
        return withOpen(stateMachine, this::realOpen);
    }
    
    ConsoleImpl(Metalog.Config config) {
        this.config = configCheck(config);
        this.stateMachine = Idempotent.createStateMachine(config.contracts());
        this.metaFactory = config.contracts().claim(Meta.Builder.FACTORY);
        this.errorMeta = metaFactory.get().key(CONSOLE_KEY).channel(CONSOLE_ERROR_CHANNEL);
        this.outputMeta = metaFactory.get().key(CONSOLE_KEY).channel(CONSOLE_OUTPUT_CHANNEL);
        fillChannelPrintStreamMap();
    }
    
    private void fillChannelPrintStreamMap() {
        channelPrintStreamMap.put(SYSTEM_OUT_CHANNEL, System.out);
        channelPrintStreamMap.put(SYSTEM_ERR_CHANNEL, System.err);
        channelPrintStreamMap.put(CONSOLE_OUTPUT_CHANNEL, System.out);
        channelPrintStreamMap.put(CONSOLE_ERROR_CHANNEL, System.err);
    }
    
    private AutoClose realOpen() {
        metalog = config.contracts().claim(Metalog.CONTRACT);
        closeSubscription = config.contracts().claim(Metalog.CONTRACT).subscribe(this);
        return this::close;
    }
    
    private void close() {
        withClose(stateMachine, this::realClose);
    }
       
   private void realClose() {
        ofNullable(closeSubscription).ifPresent(close -> {
            closeSubscription = null;
            close.close();
        });
    }
    
    private boolean isSupported(Meta meta) {
        return channelPrintStreamMap.containsKey(meta.getChannel());
    }
    
    private static final String CONSOLE_KEY = "Console";
    private static final String CONSOLE_OUTPUT_CHANNEL = "Console.output";
    private static final String CONSOLE_ERROR_CHANNEL = "Console.error";
    private static final String SYSTEM_ERR_CHANNEL = "System.err";
    private static final String SYSTEM_OUT_CHANNEL = "System.out";
    
    private final Map<String, PrintStream> channelPrintStreamMap = new HashMap<>();
    private final Filterable filters = new FiltersImpl();
    private final StateMachine<Idempotent> stateMachine;
    private final Metalog.Config config;
    private final Meta errorMeta;
    private final Meta outputMeta;
    private Metalog metalog;
    private AutoClose closeSubscription;
    private final Supplier<Meta.Builder<?>> metaFactory;
}

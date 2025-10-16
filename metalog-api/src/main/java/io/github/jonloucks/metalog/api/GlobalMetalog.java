package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.AutoClose;

import java.util.Optional;
import java.util.function.Consumer;

import static io.github.jonloucks.contracts.api.Checks.nullCheck;
import static java.util.Optional.ofNullable;

/**
 * Globally shared Metalog singleton
 */
public final class GlobalMetalog {

    /**
     * Publish a log message with the default meta information.
     * This is minimal log message, there will be no timestamp, thread info, etc.
     * @param log the log message to publish
     * @see Metalog#publish(Log)
     */
    public static void publish(Log log) {
        INSTANCE.metalog.publish(log);
    }

    /**
     * Publish a log message and it's included meta information.
     * @param log the log message to publish
     * @param meta the meta information for the given log
     * @see Metalog#publish(Log, Meta)
     */
    public static void publish(Log log, Meta meta) {
        INSTANCE.metalog.publish(log, meta);
    }

    /**
     * Publish a log message with the meta initialized in a callback
     * @param log the log message to publish
     * @param builderConsumer the callback to accept the Meta.Builder
     * @see Metalog#publish(Log, Consumer)
     */
    public static void publish(Log log, Consumer<Meta.Builder<?>> builderConsumer) {
        INSTANCE.metalog.publish(log, builderConsumer);
    }

    /**
     * Add a new log subscription
     * @param subscriber the subscriber to add
     * @return calling close removes the subscription
     * @see Metalog#subscribe(Subscriber)
     */
    public static AutoClose subscribe(Subscriber subscriber) {
        return INSTANCE.metalog.subscribe(subscriber);
    }
    
    /**
     * Return the global instance of Contracts
     * @return the instance
     */
    public static Metalog getInstance() {
        return INSTANCE.metalog;
    }
    
    /**
     * @param config the Metalog configuration
     * @return the new Metalog
     * @see MetalogFactory#create(Metalog.Config)
     * Note: Services created from this method are destink any that used internally
     * <p>
     * Caller is responsible for invoking open() before use and close when no longer needed
     * </p>
     */
    public static Metalog createMetalog(Metalog.Config config) {
        final MetalogFactory factory = findMetalogFactory(config)
            .orElseThrow(() -> new IllegalArgumentException("Metalog factory must be present."));
   
        return nullCheck(factory.create(config), "Metalog could not be created.");
    }
    
    /**
     * Finds the MetalogFactory implementation
     * @param config the configuration used to find the factory
     * @return the factory if found
     */
    public static Optional<MetalogFactory> findMetalogFactory(Metalog.Config config) {
        return ofNullable(new MetalogFactoryFinder(config).find());
    }
    
    private GlobalMetalog() {
        this.metalog = createMetalog(Metalog.Config.DEFAULT);
        this.close = metalog.open();
    }
    
    private static final GlobalMetalog INSTANCE = new GlobalMetalog();
    
    private final Metalog metalog;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final AutoClose close;
}

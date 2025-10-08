package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.AutoClose;

import java.util.function.Consumer;

import static io.github.jonloucks.contracts.api.Checks.configCheck;
import static io.github.jonloucks.contracts.api.Checks.nullCheck;

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
        final Metalog.Config validConfig = configCheck(config);
        final MetalogFactoryFinder factoryFinder = new MetalogFactoryFinder(config);
        final MetalogFactory factory = nullCheck(factoryFinder.find(), "Metalog factory must be present.");
        final Metalog metalog = factory.create(validConfig);
        
        return nullCheck(metalog, "Metalog could not be created.");
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

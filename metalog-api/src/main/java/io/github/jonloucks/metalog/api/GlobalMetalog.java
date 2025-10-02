package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.AutoClose;

import java.util.function.Consumer;

import static io.github.jonloucks.contracts.api.Checks.nullCheck;

/**
 * Globally shared Metalog singleton
 */
public final class GlobalMetalog {
    
    /**
     * @see Metalog#publish(Log)
     */
    public static void publish(Log log) {
        INSTANCE.metalog.publish(log);
    }
    
    /**
     * @see Metalog#publish(Log, Meta)
     */
    public static void publish(Log log, Meta meta) {
        INSTANCE.metalog.publish(log, meta);
    }
    
    /**
     * @see Metalog#publish(Log, Consumer)
     */
    public static void publish(Log log, Consumer<Meta.Builder<?>> metaBuilder) {
        INSTANCE.metalog.publish(log, metaBuilder);
    }
 
    /**
     * @see Metalog#isEnabled()
     */
    public static boolean isEnabled() {
        return INSTANCE.metalog.isEnabled();
    }
    
    /**
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
     * @param config the service configuration
     * @return the new service
     * @see MetalogFactory#create(Metalog.Config)
     * Create a standalone Contracts service.
     * Note: Services created from this method are destink any that used internally
     * <p>
     * Caller is responsible for invoking open() before use and close when no longer needed
     * </p>
     */
    public static Metalog createMetalog(Metalog.Config config) {
        final Metalog.Config validConfig = nullCheck(config, "Metalog config was null");
        final MetalogFactoryFinder factoryFinder = new MetalogFactoryFinder(config);
        final MetalogFactory factory = nullCheck(factoryFinder.find(), "find() was null");
        final Metalog service = factory.create(validConfig);
        
        return nullCheck(service, "createService() was null");
    }
    
    private GlobalMetalog() {
        this.metalog = createMetalog(new Metalog.Config() {});
        this.close = metalog.open();
    }
    
    private static final GlobalMetalog INSTANCE = new GlobalMetalog();
    
    private final Metalog metalog;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final AutoClose close;
}

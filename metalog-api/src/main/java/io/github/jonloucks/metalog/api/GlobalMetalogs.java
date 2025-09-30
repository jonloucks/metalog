package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.AutoClose;

import java.util.function.Consumer;

import static io.github.jonloucks.contracts.api.Checks.nullCheck;

public final class GlobalMetalogs {
    
    /**
     * @see Metalogs#publish(Log)
     */
    public static void publish(Log log) {
        INSTANCE.metalogs.publish(log);
    }
    
    /**
     * @see Metalogs#publish(Log, Meta)
     */
    public static void publish(Log log, Meta meta) {
        INSTANCE.metalogs.publish(log, meta);
    }
    
    /**
     * @see Metalogs#publish(Log, Consumer)
     */
    public static void publish(Log log, Consumer<Meta.Builder<?>> metaBuilder) {
        INSTANCE.metalogs.publish(log, metaBuilder);
    }
 
    /**
     * @see Metalogs#isEnabled()
     */
    public static boolean isEnabled() {
        return INSTANCE.metalogs.isEnabled();
    }
    
    /**
     * @see Metalogs#subscribe(Subscriber)
     */
    public static AutoClose subscribe(Subscriber subscriber) {
        return INSTANCE.metalogs.subscribe(subscriber);
    }
    
    /**
     * Return the global instance of Contracts
     * @return the instance
     */
    public static Metalogs getInstance() {
        return INSTANCE.metalogs;
    }
    
    /**
     * @param config the service configuration
     * @return the new service
     * @see MetalogsFactory#create(Metalogs.Config)
     * Create a standalone Contracts service.
     * Note: Services created from this method are destink any that used internally
     * <p>
     * Caller is responsible for invoking open() before use and close when no longer needed
     * </p>
     */
    public static Metalogs createMetalogs(Metalogs.Config config) {
        final Metalogs.Config validConfig = nullCheck(config, "Metalogs config was null");
        final MetalogsFactoryFinder factoryFinder = new MetalogsFactoryFinder(config);
        final MetalogsFactory factory = nullCheck(factoryFinder.find(), "find() was null");
        final Metalogs service = factory.create(validConfig);
        
        return nullCheck(service, "createService() was null");
    }
    
    private GlobalMetalogs() {
        this.metalogs = createMetalogs(new Metalogs.Config() {});
        this.close = metalogs.open();
    }
    
    private static final GlobalMetalogs INSTANCE = new GlobalMetalogs();
    
    private final Metalogs metalogs;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final AutoClose close;
}

package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.*;

import java.time.Duration;

/**
 * The Metalog API
 *
 * @see io.github.jonloucks.metalog.api.Publisher
 */
public interface Metalog extends Publisher, AutoOpen {
    /**
     * Access the current Metalog implementation
     */
    Contract<Metalog> CONTRACT = Contract.create(Metalog.class);
    
    /**
     * Add a new log subscription
     * @param subscriber the subscriber to add
     * @return calling close removes the subscription
     */
    AutoClose subscribe(Subscriber subscriber);
    
    /**
     * The configuration used to create a new Metalog instance.
     */
    interface Config {
        
        /**
         * The default configuration used when creating a new Metalog instance
         */
        Config DEFAULT = new Config() {};
        
        /**
         * @return if true, reflection might be used to locate the MetalogFactory
         */
        default boolean useReflection() {
            return true;
        }
        
        /**
         * @return the class name to use if reflection is used to find the MetalogFactory
         */
        default String reflectionClassName() {
            return "io.github.jonloucks.metalog.impl.ServiceFactoryImpl";
        }
        
        /**
         * @return if true, the ServiceLoader might be used to locate the MetalogFactory
         */
        default boolean useServiceLoader() {
            return true;
        }
        
        /**
         * @return the class name to load from the ServiceLoader to find the MetalogFactory
         */
        default Class<? extends MetalogFactory> serviceLoaderClass() {
            return MetalogFactory.class;
        }
        
        /**
         * @return the contracts, custom deployments may choose to not use the {@link GlobalContracts#getInstance()}
         */
        default Contracts contracts() {
            return GlobalContracts.getInstance();
        }
        
        /**
         * The maximum number of background threads dispatching log messages to subscribers.
         * @return the maximum number of background threads
         */
        default int backlogThreadCount() {
            return 5;
        }
        
        /**
         * How long to wait for logging to shut down before giving up
         * @return the timeout duration
         */
        default Duration shutdownTimeout() {
            return Duration.ofSeconds(60);
        }
        
        /**
         * When true, the default, the system output subscriber is activated and
         * log messages with channels targeting the system will be published and consumed
         * For example channels "System.err", "System.out" and "Console"
         * @return true to activate system output.
         */
        default boolean systemOutput() {
            return true;
        }
    }
}

package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.*;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * The Metalog API
 *
 * @see io.github.jonloucks.metalog.api.Publisher
 */
public interface Metalog extends Publisher, Filterable, AutoOpen {
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
            return "io.github.jonloucks.metalog.impl.MetalogFactoryImpl";
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
         * @return the contracts, some use case have their own Contracts instance.
         */
        default Contracts contracts() {
            return GlobalContracts.getInstance();
        }
        
        /**
         * The limit on how many log messages can be queued for processing
         * If the subscribers get behind.
         * Note: It is possible to run out of memory if this value is too large.
         * If the value is too small, for example 1, then every keyed log message will block
         * until the previous message has been consumed.
         * @return the limit.
         */
        default int keyedQueueLimit() {
            return 1_000;
        }
        
        /**
         * The maximum number of background threads dispatching log messages to subscribers.
         * @return the maximum number of background threads
         */
        default int unkeyedThreadCount() {
            return 10;
        }
        
        /**
         * true if all thread log messages are processed in FIFO order
         * @return true if all thread log messages are processed in FIFO order
         */
        default boolean unkeyedFairness() {
            return false;
        }
        
        /**
         * How long to wait for logging to shut down before giving up
         * @return the timeout duration
         */
        default Duration shutdownTimeout() {
            return Duration.ofSeconds(60);
        }
        
        interface Builder extends Config {
            Contract<Supplier<Builder>> FACTORY = Contract.create("Metalog Config Builder Factory");
            
            Builder useReflection(boolean useReflection);
            Builder useServiceLoader(boolean useServiceLoader);
            Builder contracts(Contracts contracts);
            Builder keyedQueueLimit(int keyedQueueLimit);
            Builder unkeyedThreadCount(int unkeyedThreadCount);
            Builder unkeyedFairness(boolean unkeyedFairness);
            Builder shutdownTimeout(Duration shutdownTimeout);
            Builder reflectionClassName(String reflectionClassName);
            Builder serviceLoaderClass(Class<? extends MetalogFactory> serviceLoaderClass);
        }
    }
}

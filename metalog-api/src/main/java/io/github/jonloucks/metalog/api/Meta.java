package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.Contract;

import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Optional.empty;

/**
 * The meta information for a log message
 */
public interface Meta extends Entity {
    
    /**
     * Can be used when logging without any meta information
     */
    Meta DEFAULT = () -> "";
    
    /**
     * User defined value to help subscribers determine how to process a log message
     * Common values would be "info", "warn", "error", "trace", "debug"
     * In addition System err and System out get redirected to System
     * @return the channel
     */
    default String getChannel() {
        return "info";
    }
    
    /**
     * Optional key to ensure ordering of messages.
     * If provided, all received messages will be consumed on the same thread.
     * @return the optional key
     */
    default Optional<String> getKey() {
        return empty();
    }
    
    /**
     * if true the {@link Log#get()} will be invoked immediately (if needed). The Metalog implementation
     * may choose to still dispatch the log on worker threads, but {@link Log#get()} will never
     * be invoked again.
     * <p>
     * Note: This is useful for contextual stateful information that may not be available
     * in the future.  For example thread local data or database sessions
     * </p>
     * @return true if blocking, the default is false
     */
    default boolean isBlocking() {
        return false;
    }
    
    /**
     * The optional time the log message was published
     * <p>
     * Note: Although the default provided here does not return a value,
     * the value will likely will be stored in the entity tree structure.
     * Locating this value is a common use case and promoted to explicit method for convenience.
     * </p>
     * @return the optional time
     */
    default Optional<Temporal> getTime() {
        return empty();
    }
    
    /**
     * The optional exception correlated to a log message
     * <p>
     * Note: Although the default provided here does not return a value,
     * the value will likely will be stored in the entity tree structure.
     * Locating this value is a common use case and promoted to explicit method for convenience.
     * </p>
     * @return the optional time
     */
    default Optional<Throwable> getThrown() {
        return empty();
    }
    
    /**
     * The optional thread correlated to a log message
     *<p>
     * Note: Although the default provided here does not return a value,
     * the value will likely will be stored in the entity tree structure.
     * Locating this value is a common use case and promoted to explicit method for convenience.
     * </p>
     * @return the optional time
     */
    default Optional<Thread> getThread() {
        return empty();
    }
    
    /**
     * Responsible for providing an easy way to build a meta instance used when logging
     * @param <B> the builder type
     */
    interface Builder<B extends Builder<B> & Entity.Builder<B>> extends Meta, Entity.Builder<B> {
        
        /**
         * Used to promise and claim the Meta.Builder implementation
         */
        Contract<Supplier<Meta.Builder<?>>> FACTORY = Contract.create("Meta Builder Factory");
        
        /**
         * Set the name for the Meta
         * @param name the name for the Meta
         * @return this builder
         */
        B name(String name);
        
        /**
         * Set the id for the Meta
         * @param id the id for the Meta
         * @return this builder
         */
        B id(String id);
        
        /**
         * Set the channel for the Meta
         * @param channel the channel for the Meta
         * @return this builder
         */
        B channel(String channel);
        
        /**
         * Set the key for the Meta
         * @param key the key for the Meta
         * @return this builder
         */
        B key(String key);
        
        /**
         * Set the block mode for the Meta
         * @param block true will enable {@link Meta#isBlocking()}
         * @return this builder
         */
        B block(boolean block);
        
        /**
         * Enables block mode. Shortcut for calling {@link #block(boolean)} with true
         * @return this builder
         */
        default B block() {
            return block(true);
        }
        
        /**
         * Set the throwable for the Meta
         * @param thrown the thrown exception
         * @return this builder
         */
        B thrown(Throwable thrown);
        
        /**
         * Set the timestamp for the Meta
         * @param timestamp the timestamp
         * @return this builder
         */
        B time(Temporal timestamp);
        
        /**
         * Set the timestamp to the current time
         * @return this builder
         */
        default B time() {
            return time(Instant.now());
        }
        
        /**
         * Set the thread for the Meta
         * @param thread the Thread
         * @return this builder
         */
        B thread(Thread thread);
        
        /**
         * Set the thread to the current runtime thread
         * @return this builder
         */
        default B thread() {
            return thread(Thread.currentThread());
        }
        
        /**
         * Copy the non-empty values from the given Meta
         * @param meta the Meta to copy
         * @return this builder
         */
        B copy(Meta meta);
    }
}

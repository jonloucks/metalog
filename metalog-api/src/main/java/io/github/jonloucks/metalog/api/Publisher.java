package io.github.jonloucks.metalog.api;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 *  Responsible for publishing log messages
 */
public interface Publisher extends Predicate<Meta> {
    
    /**
     * Publish a log message with the default meta information.
     * This is minimal log message, there will be no timestamp, thread info, etc.
     * @param log the log message to publish
     */
    default Outcome publish(Log log) {
        return publish(log, Meta.DEFAULT);
    }
    
    /**
     * Publish a log message and it's included meta information.
     * @param log the log message to publish
     * @param meta the meta information for the given log
     */
    Outcome publish(Log log, Meta meta);
    
    /**
     * Publish a log message with the meta initialized in a callback
     * @param log the log message to publish
     * @param builderConsumer the callback to accept the Meta.Builder
     */
    Outcome publish(Log log, Consumer<Meta.Builder<?>> builderConsumer);
    
    /**
     * Test of Meta matches criteria for logging
     * @param meta the meta to check
     * @return true if Meta matches criteria
     */
    default boolean test(Meta meta) {
        return true;
    }
}

package io.github.jonloucks.metalog.api;

import java.util.function.Predicate;

/**
 * Responsible for consuming published log messages and meta information.
 */
@FunctionalInterface
public interface Subscriber extends Predicate<Meta> {
    
    /**
     * Invoked for each published log message
     *
     * @param log the log message
     * @param meta the meta information
     * @return the outcome of processing the log
     */
    Outcome receive(Log log, Meta meta);
    
    /**
     * Used to short circuit needless processing
     * @param meta the Meta to check
     * @return true if Meta matches
     */
    @Override
    default boolean test(Meta meta) {
        return true;
    }
}

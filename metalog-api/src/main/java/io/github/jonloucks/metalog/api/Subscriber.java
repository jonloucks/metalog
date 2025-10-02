package io.github.jonloucks.metalog.api;

/**
 * Responsible for consuming published log messages and meta information.
 */
@FunctionalInterface
public interface Subscriber {
    
    /**
     * Invoked for each published log message
     *
     * @param log the log message
     * @param meta the meta information
     */
    void receive(Log log, Meta meta);
}

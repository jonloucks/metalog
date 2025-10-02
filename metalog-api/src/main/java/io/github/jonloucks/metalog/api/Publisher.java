package io.github.jonloucks.metalog.api;

import java.util.function.Consumer;

/**
 *  Responsible for publishing log messages
 */
public interface Publisher  {
    
    /**
     * Publish a log message with the default meta information.
     * This is minimal log message, there will be no timestamp, thread info, etc.
     * @param log the log message to publish
     */
    default void publish(Log log) {
        publish(log, Meta.DEFAULT);
    }
    
    /**
     * Publish a log message and it's included meta information.
     * @param log the log message to publish
     * @param meta the meta information for the given log
     */
    void publish(Log log, Meta meta);
    
    /**
     * Publish a log message with the meta initialized in the metaBuilder callback
     * @param log the log message to publish
     * @param metaBuilder the callback to accept the Meta.Builder
     */
    void publish(Log log, Consumer<Meta.Builder<?>> metaBuilder);
    
    /**
     * @return true if the publisher is enabled, if false all published logs must be discarded
     */
    default boolean isEnabled() {
        return true;
    }
}

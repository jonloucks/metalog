package io.github.jonloucks.metalog.api;

/**
 * Publish outcode
 */
public enum Outcome {
    /**
     * Known consumption of the log message
     */
    CONSUMED,
    /**
     * Dispatched asynchronously and will likely be consumed in the future
     */
    DISPATCHED,
    /**
     * Skipped because of filters or no subscribers are interested in the Meta
     */
    SKIPPED,
    /**
     * The publisher can't fulfill the request, for example during shutdown
     */
    REJECTED
}

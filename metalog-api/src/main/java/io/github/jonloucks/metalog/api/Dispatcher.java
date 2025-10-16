package io.github.jonloucks.metalog.api;


import io.github.jonloucks.contracts.api.AutoOpen;
import io.github.jonloucks.contracts.api.Contract;

import java.util.function.Supplier;

/**
 * Responsible for delegating processing log messages dispatching
 */
public interface Dispatcher extends AutoOpen {
    /**
     * Dispatcher tuned for Log messages with sequence keys, normally a one-to-one relationship.
     */
    Contract<Supplier<Dispatcher>> KEYED_FACTORY = Contract.create("Keyed Dispatcher Factory");
    
    /**
     * Dispatcher tuned for Log messages, normally all unkeys mapped to many worker threads
     */
    Contract<Supplier<Dispatcher>> UNKEYED_FACTORY = Contract.create("Unkeyed Dispatcher Factory");
    
    /**
     * Responsible for delegating processing log messages dispatching
     * @param meta the meta of the log message
     * @param job the job that transmits the log message
     * @return the outcome
     */
    Outcome dispatch(Meta meta, Runnable job);
}

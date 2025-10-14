package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.Contract;

/**
 * Responsible for the special logging case of System out and System err
 * It is both a Subscriber and Publisher
 */
public interface Console extends Publisher, Subscriber, Filterable {
    
    /**
     * The Contract for the Console.
     * It is replaceable for scenarios where the Console output needs to redirected
     * from System err and System out
     */
    Contract<Console> CONTRACT = Contract.create(Console.class);
    
    @Override
    boolean test(Meta meta);
    
    @Override
    Outcome publish(Log log);
    /**
     * Publishes the log with Console info meta
     * @param log the log to publish
     * @return the outcome
     */
    Outcome output(Log log);
    
    /**
     * Publishes the log with Console error meta
     * @param log the log to publish
     * @return the outcome
     */
    Outcome error(Log log);
}

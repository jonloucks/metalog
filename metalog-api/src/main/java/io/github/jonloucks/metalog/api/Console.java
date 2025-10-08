package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.Contract;

/**
 * Responsible for the special logging case of System.out and System.err
 * It is both a Subscriber and Publisher
 */
public interface Console extends Subscriber, Publisher, Filterable {
    
    /**
     * The Contract for the Console.
     * It is replaceable for scenarios where the Console output needs to redirected
     * from System.err and System.out
     */
    Contract<Console> CONTRACT = Contract.create(Console.class, b -> b.replaceable(true));
}

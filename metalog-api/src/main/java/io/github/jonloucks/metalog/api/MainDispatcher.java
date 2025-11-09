package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.Contract;

/**
 * Responsibility: Create delegate dispatchers on demand and discard them when no longer needed
 */
public interface MainDispatcher extends Dispatcher {
    /**
     * The Contract for the main dispatcher
     */
    Contract<MainDispatcher> CONTRACT = Contract.create(MainDispatcher.class);
    
    /**
     * Uses the Subscriber to determine which dispatchers are required
     * @param subscriber the subscriber
     */
    void registerSubscriber(Subscriber subscriber);
    
    /**
     * Used the Subscriber to determine which dispatchers are no longer needed
     * @param subscriber the subscriber
     */
    void unregisterSubscriber(Subscriber subscriber);
}

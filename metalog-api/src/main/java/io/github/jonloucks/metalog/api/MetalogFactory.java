package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.AutoOpen;
import io.github.jonloucks.contracts.api.Contract;

/**
 * Responsible for creating new instances of Metalog
 */
public interface MetalogFactory {
    /**
     * Used to promise and claim the MetalogFactory implementation
     */
    Contract<MetalogFactory> CONTRACT = Contract.create(MetalogFactory.class);
    
    /**
     * Create a new instance of Metalog
     * <p>
     *     Note: caller is responsible for calling {@link AutoOpen#open()} and calling
     *     the {@link io.github.jonloucks.contracts.api.AutoClose#close() when done}
     * </p>
     * @param config the Metalog configuration for the new instance
     * @return the new Metalog instance
     */
    Metalog create(Metalog.Config config);
}

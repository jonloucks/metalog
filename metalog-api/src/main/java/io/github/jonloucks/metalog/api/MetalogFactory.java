package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.*;

import java.util.function.Consumer;

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
    
    Metalog create(Consumer<Metalog.Config.Builder> builderConsumer);
    
    void install(Metalog.Config config, Repository repository);
}

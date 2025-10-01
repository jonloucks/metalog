package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.Contract;

public interface MetalogFactory {
    Contract<MetalogFactory> CONTRACT = Contract.create(MetalogFactory.class);
    
    Metalog create(Metalog.Config config);
}

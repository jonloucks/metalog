package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.Contract;

public interface MetalogsFactory {
    Contract<MetalogsFactory> CONTRACT = Contract.create(MetalogsFactory.class);
    
    Metalogs create(Metalogs.Config config);
}

package io.github.jonloucks.metalog.impl;


import io.github.jonloucks.metalog.api.Metalog;
import io.github.jonloucks.metalog.api.MetalogFactory;

/**
 * Creates Metalog instances
 * Opt-in construction via reflection or ServiceLoader
 */
public final class MetalogFactoryImpl implements MetalogFactory {
    public MetalogFactoryImpl() {
    }
    
    @Override
    public Metalog create(Metalog.Config config) {
        return new MetalogImpl(config);
    }
}

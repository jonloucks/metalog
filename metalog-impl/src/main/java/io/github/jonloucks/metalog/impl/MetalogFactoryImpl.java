package io.github.jonloucks.metalog.impl;


import io.github.jonloucks.metalog.api.Metalog;
import io.github.jonloucks.metalog.api.MetalogFactory;

import static io.github.jonloucks.metalog.impl.Internal.configCheck;

/**
 * Creates Metalog instances
 * Opt-in construction via reflection or ServiceLoader
 */
public final class MetalogFactoryImpl implements MetalogFactory {
    @Override
    public Metalog create(Metalog.Config config) {
        final Metalog.Config validConfig = configCheck(config);

        return new MetalogImpl(validConfig);
    }
}

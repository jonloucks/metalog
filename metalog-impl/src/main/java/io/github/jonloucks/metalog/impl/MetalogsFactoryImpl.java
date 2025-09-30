package io.github.jonloucks.metalog.impl;


import io.github.jonloucks.metalog.api.Metalogs;
import io.github.jonloucks.metalog.api.MetalogsFactory;

import static io.github.jonloucks.metalog.impl.Internal.configCheck;

/**
 * Creates Metalogs instances
 * Opt-in construction via reflection or ServiceLoader
 */
public final class MetalogsFactoryImpl implements MetalogsFactory {
    @Override
    public Metalogs create(Metalogs.Config config) {
        final Metalogs.Config validConfig = configCheck(config);

        return new MetalogsImpl(validConfig);
    }
}

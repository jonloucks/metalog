package io.github.jonloucks.metalog.test;

import io.github.jonloucks.contracts.api.Repository;
import io.github.jonloucks.contracts.test.BadContractsFactoryTests;
import io.github.jonloucks.metalog.api.Metalog;
import io.github.jonloucks.metalog.api.MetalogFactory;

import java.util.function.Consumer;

/**
 * Used to introduce errors.
 * 1. Class is not public
 * 2. create throws an exception
 * 3. Constructor is not public
 * @see BadContractsFactoryTests
 */
final class BadMetalogFactory implements MetalogFactory {
    @Override
    public Metalog create(Metalog.Config config) {
        throw new UnsupportedOperationException("Not supported ever.");
    }
    
    @Override
    public Metalog create(Consumer<Metalog.Config.Builder> builderConsumer) {
        throw new UnsupportedOperationException("Not supported ever.");
    }
    
    @Override
    public void install(Metalog.Config config, Repository repository) {
        throw new UnsupportedOperationException("Not supported ever.");
    }
    
    BadMetalogFactory() {
    }
}

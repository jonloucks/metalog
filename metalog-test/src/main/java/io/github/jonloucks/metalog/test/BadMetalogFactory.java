package io.github.jonloucks.metalog.test;

import io.github.jonloucks.contracts.test.BadContractsFactoryTests;
import io.github.jonloucks.metalog.api.Metalog;
import io.github.jonloucks.metalog.api.MetalogFactory;

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
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    BadMetalogFactory() {
    }
}

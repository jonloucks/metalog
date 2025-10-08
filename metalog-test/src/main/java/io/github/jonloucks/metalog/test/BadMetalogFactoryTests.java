package io.github.jonloucks.metalog.test;

import io.github.jonloucks.metalog.api.Metalog;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static io.github.jonloucks.contracts.test.Tools.assertObject;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("CodeBlock2Expr")
public interface BadMetalogFactoryTests {
    @Test
    default void badContractsFactory_HasProtectedConstructor() throws Throwable {
        final Class<?> klass = Class.forName(BadMetalogFactory.class.getCanonicalName());
        final Constructor<?> constructor = klass.getDeclaredConstructor();
        constructor.setAccessible(true);
        final int modifiers = constructor.getModifiers();
        
        assertFalse(Modifier.isPublic(modifiers), "constructor should not be public.");
    }
    
    @Test
    default void badContractsFactory_HasPrivateConstructor() throws Throwable {
        final BadMetalogFactory badContractsFactory = new BadMetalogFactory();
        final Metalog.Config config = new Metalog.Config(){};
        final Exception thrown = assertThrows(Exception.class, () -> {
            badContractsFactory.create(config);
        });
        
        assertObject(thrown);
    }
}

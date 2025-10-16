package io.github.jonloucks.metalog.impl.test;

import io.github.jonloucks.metalog.impl.Stub;
import org.junit.jupiter.api.Test;

public interface InternalTests extends IdempotentTests {
    
    @Test
    default void internalTests() {
        Stub.validate();
    }
}

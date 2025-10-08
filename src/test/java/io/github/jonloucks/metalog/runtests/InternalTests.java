package io.github.jonloucks.metalog.runtests;

import io.github.jonloucks.metalog.Stub;
import org.junit.jupiter.api.Test;

import static io.github.jonloucks.contracts.test.Tools.assertInstantiateThrows;

public interface InternalTests {
    
    @Test
    default void stub_Instantiate_Throws() {
        assertInstantiateThrows(Stub.class);
    }
    
    @Test
    default void stub_validate() {
        Stub.validate();
    }
}

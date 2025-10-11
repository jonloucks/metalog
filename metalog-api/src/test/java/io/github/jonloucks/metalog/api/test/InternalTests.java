package io.github.jonloucks.metalog.api.test;

import io.github.jonloucks.metalog.api.GlobalMetalog;
import org.junit.jupiter.api.Test;

import static io.github.jonloucks.contracts.test.Tools.assertInstantiateThrows;

public interface InternalTests {
    
    @Test
    default void api_InstantiateGlobalMetalog_Throws() {
        assertInstantiateThrows(GlobalMetalog.class);
    }
}

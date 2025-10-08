package io.github.jonloucks.metalog.test;

import org.junit.jupiter.api.Test;

import static io.github.jonloucks.contracts.test.Tools.assertInstantiateThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SuppressWarnings("Convert2MethodRef")
public interface ToolsTests extends
    BadMetalogFactoryTests
{
    
    @Test
    default void testTools_Instantiate_Throws() {
        assertInstantiateThrows(Tools.class);
    }
    
    @Test
    default void testTools_clean_DoesNotThrow() {
        assertDoesNotThrow(()-> Tools.clean());
    }
}

package io.github.jonloucks.metalog.test;

import io.github.jonloucks.metalog.api.MetalogException;
import org.junit.jupiter.api.Test;

import static io.github.jonloucks.contracts.test.Tools.assertIsSerializable;
import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings({"ThrowableNotThrown", "CodeBlock2Expr"})
public interface ExceptionTests {
    
    @Test
    default void exception_MetalogException_WithNullMessage_Throws() {
        final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            new MetalogException(null);
        });
        
        assertThrown(thrown);
    }
    
    @Test
    default void exception_MetalogException_IsSerializable() {
        assertIsSerializable(MetalogException.class);
    }
    
    @Test
    default void exception_MetalogException_WithValid_Works() {
        final MetalogException exception = new MetalogException("Abc.");
        
        assertThrown(exception, "Abc.");
    }
    
    @Test
    default void exception_MetalogException_WithCause_Works() {
        final OutOfMemoryError cause = new OutOfMemoryError("Out of memory.");
        final MetalogException exception = new MetalogException("Abc.", cause);
        
        assertThrown(exception, cause, "Abc.");
    }
}

package io.github.jonloucks.metalog.test;

import io.github.jonloucks.metalog.api.Checks;
import io.github.jonloucks.metalog.api.MetalogException;
import io.github.jonloucks.contracts.api.GlobalContracts;
import org.junit.jupiter.api.Test;

import static io.github.jonloucks.metalog.test.Tools.withMetalog;
import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SuppressWarnings("CodeBlock2Expr")
public interface ValidateTests {
    
    @Test
    default void validate_WithNullContracts_Throws() {
        withMetalog((contracts, metalog) -> {
            assertThrown(IllegalArgumentException.class,
                () -> Checks.validateMetalog(null, metalog),
                "Contracts must be present.");
        });
    }
    
    @Test
    default void validate_WithNullMetalog_Throws() {
        withMetalog((contracts, metalog) -> {
            assertThrown(IllegalArgumentException.class,
                () -> Checks.validateMetalog(contracts, null),
                "Metalog must be present.");
        });
    }
    
    @Test
    default void validate_WithMetalogClaimDifferent_Throws() {
        withMetalog((contracts, metalog) -> {
            assertThrown(MetalogException.class,
                () -> Checks.validateMetalog(GlobalContracts.getInstance(), metalog),
                "Metalog.CONTRACT claim is different.");
        });
    }
    
    @Test
    default void validate_Valid_Works() {
        withMetalog((contracts, metalog) -> {
            assertDoesNotThrow(() -> Checks.validateMetalog(contracts, metalog));
        });
    }
}

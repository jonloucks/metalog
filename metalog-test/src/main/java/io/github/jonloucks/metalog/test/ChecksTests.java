package io.github.jonloucks.metalog.test;

import io.github.jonloucks.metalog.api.Checks;
import io.github.jonloucks.metalog.api.Metalog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static io.github.jonloucks.contracts.test.Tools.assertInstantiateThrows;
import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static org.junit.jupiter.api.Assertions.assertSame;

@SuppressWarnings("ResultOfMethodCallIgnored")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public interface ChecksTests {
    
    @Test
    default void checks_Instantiate_Throws() {
        assertInstantiateThrows(Checks.class);
    }
 
    @Test
    default void checks_metalogCheck_WhenNull_Throws() {
        assertThrown(IllegalArgumentException.class, () -> Checks.metalogCheck(null));
    }

    
    @Test
    default void checks_metalogCheck_WithValid_Works(@Mock Metalog metalog) {
        assertSame(metalog, Checks.metalogCheck(metalog));
    }
}

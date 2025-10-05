package io.github.jonloucks.metalog;

import io.github.jonloucks.contracts.api.Checks;
import io.github.jonloucks.contracts.api.GlobalContracts;

/**
 * A placeholder class to make sure dependencies are correct for api and implementation.
 */
public final class Stub {
 
    private Stub() {
        throw new AssertionError("Illegal constructor call.");
    }
    
    /**
     * Validates basic functionality.
     */
    public static void validate() {
        Checks.validateContracts(GlobalContracts.getInstance());
    }
}

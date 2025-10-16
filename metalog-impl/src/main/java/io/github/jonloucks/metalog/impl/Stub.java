package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.contracts.api.GlobalContracts;

/**
 * Provides runtime validation
 */
public final class Stub {
    
    private Stub() {
    }
    
    /**
     * Provides runtime validation
     */
    public static void validate() {
        io.github.jonloucks.contracts.api.Checks.validateContracts(GlobalContracts.getInstance());
    }
}

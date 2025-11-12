package io.github.jonloucks.metalog;

import io.github.jonloucks.metalog.api.Metalog;
import io.github.jonloucks.metalog.api.MetalogException;
import io.github.jonloucks.metalog.api.GlobalMetalog;
import io.github.jonloucks.contracts.api.ContractException;
import io.github.jonloucks.contracts.api.Contracts;
import io.github.jonloucks.contracts.api.GlobalContracts;

import static io.github.jonloucks.metalog.api.Checks.validateMetalog;

/**
 * A placeholder class to make sure dependencies are correct for api and implementation.
 */
public final class Stub {
    
    /**
     * Utility class instantiation protection
     * Test coverage not possible, java module protections in place
     */
    private Stub() {
        // conflicting standards.  100% code coverage vs throwing exception on instantiation of utility class.
        // Java modules protects agents invoking private methods.
        // There are unit tests that will fail if this constructor is not private
    }
    
    /**
     * Quickly validates Global Contracts and Metalog
     *
     * @throws ContractException when invalid
     * @throws MetalogException when invalid
     * @throws IllegalArgumentException when invalid
     */
    public static void validate() {
        validate(GlobalContracts.getInstance(), GlobalMetalog.getInstance());
    }
    
    /**
     * Quickly validates a Contracts and Metalog
     *
     * @param contracts the Contracts to validate
     * @param metalog the Metalog to validate
     *
     * @throws ContractException when invalid
     * @throws MetalogException when invalid
     * @throws IllegalArgumentException when invalid
     */
    public static void validate(Contracts contracts, Metalog metalog) {
        validateMetalog(contracts, metalog);
    }
}

package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.ContractException;
import io.github.jonloucks.contracts.api.Contracts;
import io.github.jonloucks.contracts.api.Repository;

import static io.github.jonloucks.contracts.api.Checks.nullCheck;
import static io.github.jonloucks.contracts.api.Checks.validateContracts;

/**
 * Checks used internally and supported for external use.
 */
public final class Checks {
    
    /**
     * Check if given Metalog is not null or invalid
     *
     * @param metalog the Metalog to check
     * @return a valid Metalog
     * @throws IllegalArgumentException when invalid
     */
    public static Metalog metalogCheck(Metalog metalog) {
        return nullCheck(metalog, "Metalog must be present.");
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
    public static void validateMetalog(Contracts contracts, Metalog metalog) {
        final Metalog validMetalog = metalogCheck(metalog);
        validateContracts(contracts);
        
        final Metalog promisedMetalog = contracts.claim(Metalog.CONTRACT);
        if (validMetalog != promisedMetalog) {
            throw new MetalogException("Metalog.CONTRACT claim is different.");
        }
        
        final Repository repository = contracts.claim(Repository.FACTORY).get();
        repository.require(MetalogFactory.CONTRACT);
        repository.require(Console.CONTRACT);
        repository.require(MainDispatcher.CONTRACT);
        repository.require(Dispatcher.KEYED_FACTORY);
        repository.require(Dispatcher.UNKEYED_FACTORY);
        
        repository.open().close();
    }
    
    
    private Checks() {
        
    }
}

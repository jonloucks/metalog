/**
 * Includes all components for the smoke app
 */
module io.github.jonloucks.metalog.smoke {
    requires transitive io.github.jonloucks.contracts;
    requires transitive io.github.jonloucks.concurrency;
    requires transitive io.github.jonloucks.metalog;
    
    uses io.github.jonloucks.contracts.api.ContractsFactory;
    uses io.github.jonloucks.concurrency.api.ConcurrencyFactory;
    uses io.github.jonloucks.metalog.api.MetalogFactory;
    
    exports io.github.jonloucks.metalog.smoke;
}
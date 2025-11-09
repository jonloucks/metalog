/**
 * The implementation module for Metalog
 */
module io.github.jonloucks.metalog.impl {
    requires transitive io.github.jonloucks.contracts.api;
    requires transitive io.github.jonloucks.concurrency.api;
    requires transitive io.github.jonloucks.metalog.api;
    
    exports io.github.jonloucks.metalog.impl;
    
    uses io.github.jonloucks.concurrency.api.ConcurrencyFactory;

    provides io.github.jonloucks.metalog.api.MetalogFactory with io.github.jonloucks.metalog.impl.MetalogFactoryImpl;
}
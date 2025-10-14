/**
 * The implementation module for Metalog
 */
module io.github.jonloucks.metalog.impl {
    requires transitive io.github.jonloucks.contracts.api;
    requires transitive io.github.jonloucks.metalog.api;
    
    opens io.github.jonloucks.metalog.impl to io.github.jonloucks.metalog.api;
    exports io.github.jonloucks.metalog.impl;
    
    provides io.github.jonloucks.metalog.api.MetalogFactory with io.github.jonloucks.metalog.impl.MetalogFactoryImpl;
}
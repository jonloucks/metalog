/**
 * The API module for Metalog
 */
module io.github.jonloucks.metalog.api {
    requires transitive io.github.jonloucks.contracts.api;
    
    uses io.github.jonloucks.metalog.api.MetalogFactory;
    
    exports io.github.jonloucks.metalog.api;
}
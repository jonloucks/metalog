import io.github.jonloucks.metalog.api.MetalogFactory;

module io.github.jonloucks.metalog.api {
    requires transitive io.github.jonloucks.contracts.api;
    
    uses MetalogFactory;
    
    exports io.github.jonloucks.metalog.api;
}
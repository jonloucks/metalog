import io.github.jonloucks.metalog.api.MetalogsFactory;

module io.github.jonloucks.metalog.api {
    requires transitive io.github.jonloucks.contracts.api;
    
    uses MetalogsFactory;
    
    exports io.github.jonloucks.metalog.api;
}
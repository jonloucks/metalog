import io.github.jonloucks.metalog.api.MetalogFactory;
import io.github.jonloucks.metalog.impl.MetalogFactoryImpl;

/**
 * Metalog default implementation
 */
module io.github.jonloucks.metalog.impl {
    requires transitive io.github.jonloucks.contracts.api;
    requires transitive io.github.jonloucks.metalog.api;
    
    provides MetalogFactory with MetalogFactoryImpl;
}
import io.github.jonloucks.metalog.api.MetalogsFactory;
import io.github.jonloucks.metalog.impl.MetalogsFactoryImpl;

/**
 * Metalogs default implementation
 */
module io.github.jonloucks.metalog.impl {
    requires transitive io.github.jonloucks.contracts.api;
    requires transitive io.github.jonloucks.metalog.api;
    
    provides MetalogsFactory with MetalogsFactoryImpl;
}
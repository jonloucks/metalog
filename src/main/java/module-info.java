/**
 * Includes all default components of the Metalog library needed for a working deployment.
 */
module io.github.jonloucks.metalog {
    requires transitive io.github.jonloucks.metalog.api;
    requires transitive io.github.jonloucks.metalog.impl;
    
    exports io.github.jonloucks.metalog;
}
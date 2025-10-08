/**
 * Includes all default components of the Metalog library needed for a working deployment.
 */
module io.github.jonloucks.metalog.runtests {
    requires transitive io.github.jonloucks.metalog.api;
    requires transitive io.github.jonloucks.metalog.impl;
    requires transitive io.github.jonloucks.metalog.test;
    requires io.github.jonloucks.metalog;
    
    opens io.github.jonloucks.metalog.runtests to org.junit.platform.commons;
    
    exports io.github.jonloucks.metalog.runtests;
}
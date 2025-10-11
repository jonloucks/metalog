/**
 * For internal tests specific to this module
 */
module io.github.jonloucks.metalog.api.test {
    
    requires transitive io.github.jonloucks.contracts.test;
    requires transitive io.github.jonloucks.metalog.api;
    
    opens io.github.jonloucks.metalog.api.test to org.junit.platform.commons;
}
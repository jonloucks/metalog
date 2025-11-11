/**
 * Includes all components for testing the smoke app
 */
module io.github.jonloucks.metalog.smoke.test {
    requires transitive io.github.jonloucks.contracts;
    requires transitive io.github.jonloucks.contracts.test;
    requires transitive io.github.jonloucks.metalog.test;
    requires transitive io.github.jonloucks.metalog.smoke;
    
    opens io.github.jonloucks.metalog.smoke.test to org.junit.platform.commons;
    
    exports io.github.jonloucks.metalog.smoke.test;
}
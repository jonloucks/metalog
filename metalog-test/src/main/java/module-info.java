/**
 * The Test module for Metalog
 */
module io.github.jonloucks.metalog.test {
    requires transitive io.github.jonloucks.metalog.api;
    requires transitive io.github.jonloucks.contracts.api;
    requires transitive io.github.jonloucks.contracts.test;
    requires transitive io.github.jonloucks.concurrency.api;
    requires transitive io.github.jonloucks.concurrency.test;
    
    requires org.junit.jupiter.api;
    requires org.mockito.junit.jupiter;
    requires org.mockito;
    
    opens io.github.jonloucks.metalog.test to org.junit.platform.commons;
    exports io.github.jonloucks.metalog.test;
}
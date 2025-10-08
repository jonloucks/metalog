/**
 * The Test module for Metalog
 */
module io.github.jonloucks.metalog.test {
    requires transitive io.github.jonloucks.metalog.api;
    requires transitive io.github.jonloucks.contracts.api;
    requires transitive io.github.jonloucks.contracts.test;

    opens io.github.jonloucks.metalog.test to org.junit.platform.commons;
    exports io.github.jonloucks.metalog.test;
}
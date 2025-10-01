module io.github.metalog.metalog.test {
    requires transitive io.github.jonloucks.metalog.api;
    requires transitive io.github.jonloucks.contracts.api;
    requires transitive io.github.jonloucks.contracts.test;
    requires transitive org.junit.jupiter.api;
    requires transitive org.mockito.junit.jupiter;
    requires transitive org.mockito;
    
    opens io.github.jonloucks.metalog.test to org.junit.platform.commons;
    exports io.github.jonloucks.metalog.test;
}
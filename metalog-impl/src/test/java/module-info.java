import io.github.jonloucks.metalog.api.MetalogsFactory;

module io.github.jonloucks.metalog.impl.test {
    requires transitive io.github.jonloucks.contracts;
    requires transitive io.github.jonloucks.contracts.test;
    requires transitive io.github.jonloucks.metalog.api;
    requires transitive io.github.metalog.metalog.test;
    requires transitive io.github.jonloucks.metalog.impl;

    uses MetalogsFactory;
    
    exports io.github.jonloucks.metalog.impl.test to org.junit.platform.commons;
}
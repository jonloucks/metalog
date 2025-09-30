import io.github.jonloucks.metalog.api.MetalogFactory;

module io.github.jonloucks.metalog.impl.test {
    requires transitive io.github.jonloucks.contracts;
    requires transitive io.github.jonloucks.contracts.test;
    requires transitive io.github.jonloucks.metalog.api;
    requires transitive io.github.metalog.metalog.test;
    requires transitive io.github.jonloucks.metalog.impl;

    uses MetalogFactory;
    
    exports io.github.jonloucks.metalog.impl.test to org.junit.platform.commons;
}
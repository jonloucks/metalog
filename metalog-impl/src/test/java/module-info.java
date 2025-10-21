/**
 * module-impl tests
 */
module io.github.jonloucks.metalog.impl.test {
    requires transitive io.github.jonloucks.contracts;
    requires transitive io.github.jonloucks.contracts.test;
    requires transitive io.github.jonloucks.concurrency;
    requires transitive io.github.jonloucks.concurrency.test;
    requires transitive io.github.jonloucks.metalog.api;
    requires transitive io.github.jonloucks.metalog.test;
    requires transitive io.github.jonloucks.metalog.impl;

    uses io.github.jonloucks.metalog.api.MetalogFactory;
    
    opens io.github.jonloucks.metalog.impl.test to org.junit.platform.commons;
}
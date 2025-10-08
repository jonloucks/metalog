/**
 * Module to run tests on the test tools
 */
module io.github.jonloucks.metalog.test.run {
    requires transitive io.github.jonloucks.metalog.test;
    
    opens io.github.jonloucks.metalog.test.run to org.junit.platform.commons;
}
module io.github.metalog.metalog.test.run {
    requires transitive io.github.metalog.metalog.test;
    
    opens io.github.jonloucks.metalog.test.run to org.junit.platform.commons;
}
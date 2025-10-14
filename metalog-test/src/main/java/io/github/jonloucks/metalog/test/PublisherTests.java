package io.github.jonloucks.metalog.test;

import io.github.jonloucks.metalog.api.Log;
import io.github.jonloucks.metalog.api.Meta;
import io.github.jonloucks.metalog.api.Outcome;
import io.github.jonloucks.metalog.api.Publisher;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;

public interface PublisherTests {
    
    @Test
    default void publisher_Defaults() {
        final Publisher publisher = new Publisher(){
            @Override
            public Outcome publish(Log log, Meta meta) {
                return Outcome.SKIPPED;
            }
            
            @Override
            public Outcome publish(Log log, Consumer<Meta.Builder<?>> builderConsumer) {
                return Outcome.SKIPPED;
            }
        };
        assertTrue(publisher.test(Meta.DEFAULT));
    }
}

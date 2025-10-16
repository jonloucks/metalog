package io.github.jonloucks.metalog.test;

import io.github.jonloucks.metalog.api.Dispatcher;
import io.github.jonloucks.metalog.api.Meta;
import io.github.jonloucks.metalog.api.Outcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static io.github.jonloucks.metalog.test.Tools.withMetalog;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public interface UnkeyedDispatcherTests {
    
    @Test
    default void unkeyedDispatcher_BeforeOpen(@Mock Runnable runnable) {
        withMetalog(b -> {}, (contracts, metalog) -> {
            final Dispatcher dispatcher = contracts.claim(Dispatcher.UNKEYED_FACTORY).get();
            final Outcome outcome = dispatcher.dispatch(Meta.DEFAULT, runnable);
            
            if (outcome == Outcome.REJECTED) {
                verify(runnable, times(0)).run();
            } else if (outcome == Outcome.CONSUMED){
                verify(runnable, times(1)).run();
            } else {
                fail("Unexpected outcome: " + outcome);
            }
        });
    }
}

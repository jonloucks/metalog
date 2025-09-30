package io.github.jonloucks.metalog.test;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.metalog.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.util.function.Consumer;

import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static io.github.jonloucks.contracts.test.Tools.sleep;
import static io.github.jonloucks.metalog.api.GlobalMetalogs.createMetalogs;
import static io.github.jonloucks.metalog.test.MetalogsTests.SmokeTestsTools.runWithScenario;
import static io.github.jonloucks.metalog.test.Tools.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public interface MetalogsTests {
    
    @Test
    default void metalogs_addFilter_WithNull_Throws() {

        runWithScenario(metalogs -> {
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                //noinspection resource
                metalogs.addFilter(null);
            });
            
            assertThrown(thrown);
        });
    }
    
    @Test
    default void metalogs_WithThrown_Works(@Mock Subscriber subscriber, @Mock Log log) {
        final String text = uniqueString();
        when(log.get()).thenReturn(text);
        final String id = uniqueString();
        
        runWithScenario(metalogs -> {
            try (AutoClose closeSubscriber = metalogs.subscribe(subscriber)) {
                final AutoClose ignored = closeSubscriber;
                final Throwable thrown = new ArithmeticException("oops");
                
                metalogs.publish(log, b -> b.id(id).thrown(thrown));
                
                sleep(Duration.ofSeconds(1));
                
                verify(subscriber, times(1)).receive(any(), metaWithIdThrown(id, thrown));
            }
        });
    }
    
    @Test
    default void metalogs_subscribe_close_while_queued_Works(@Mock Subscriber subscriber, @Mock Log log) {
        final String text = uniqueString();
        when(log.get()).thenReturn(text);
        final String id = uniqueString();
        
        runWithScenario(metalogs -> {
            try (AutoClose closeSubscriber = metalogs.subscribe(subscriber)) {
                final AutoClose ignored = closeSubscriber;
                
                metalogs.publish(log, b -> b.id(id));
            }
            
            // subscription is closed and should no longer receive logs
            metalogs.publish(log, b -> b.id(id));
            sleep(Duration.ofSeconds(1));
            verify(subscriber, times(0)).receive(any(), metaWithId(id));
        });
    }
    
    @Test
    default void metalogs_subscribe_close_Works(@Mock Subscriber subscriber, @Mock Log log) {
        final String text = uniqueString();
        when(log.get()).thenReturn(text);
        final String id = uniqueString();
        
        runWithScenario(metalogs -> {
            try (AutoClose closeSubscriber = metalogs.subscribe(subscriber)) {
                final AutoClose ignored = closeSubscriber;
                
                metalogs.publish(log, b -> b.id(id));
                sleep(Duration.ofSeconds(1));
            }
            
            // subscription is closed and should no longer receive logs
            metalogs.publish(log, b -> b.id(id));
            sleep(Duration.ofSeconds(1));
            verify(subscriber, times(1)).receive(any(), metaWithId(id));
        });
    }
    
    final class SmokeTestsTools {
        private SmokeTestsTools() {
        }

        interface ScenarioConfig extends Consumer<Metalogs>{
            default Metalogs.Config getMetalogsConfig() {
                return Metalogs.Config.DEFAULT;
            }
        }
        
        static void runWithScenario(ScenarioConfig scenarioConfig) {
            final Metalogs metalogs = createMetalogs(scenarioConfig.getMetalogsConfig());
            try (AutoClose closeLogs = metalogs.open()) {
                AutoClose ignoreWarning = closeLogs;
                scenarioConfig.accept(metalogs);
            }
        }
    }
}

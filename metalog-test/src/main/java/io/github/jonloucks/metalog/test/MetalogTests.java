package io.github.jonloucks.metalog.test;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.metalog.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

import java.util.function.Consumer;

import static io.github.jonloucks.contracts.test.Tools.assertInstantiateThrows;
import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static io.github.jonloucks.metalog.api.GlobalMetalog.createMetalog;
import static io.github.jonloucks.metalog.test.MetalogTests.MetalogTestsTools.runWithScenario;
import static io.github.jonloucks.metalog.test.Tools.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public interface MetalogTests {
    
    @Test
    default void metalog_addFilter_WithNull_Throws() {

        runWithScenario(metalog -> {
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                //noinspection resource
                metalog.addFilter(null);
            });
            
            assertThrown(thrown);
        });
    }
    
    @Test
    default void metalog_WithThrown_Works(@Mock Subscriber subscriber, @Mock Log log) {
        final String text = uniqueString();
        when(log.get()).thenReturn(text);
        when(subscriber.test(any())).thenReturn(true);
        final String id = uniqueString();
        
        doAnswer((Answer<Void>) invocation -> {
            final Log passedLog = (Log)invocation.getArguments()[0];
            passedLog.get();
            passedLog.get();
            passedLog.get();
            return null; // Void methods return null in doAnswer
        }).when(subscriber).receive(any(), any());
        
        runWithScenario(metalog -> {
            try (AutoClose closeSubscriber = metalog.subscribe(subscriber)) {
                final AutoClose ignored = closeSubscriber;
                final Throwable thrown = new ArithmeticException("Oops.");
                
                metalog.publish(log, b -> b.id(id).thrown(thrown).block());
     
                verify(subscriber, times(1)).receive(any(), metaWithIdThrown(id, thrown));
            }
        });
    }
    
    @Test
    default void metalog_subscribe_close_while_queued_Works(@Mock Subscriber subscriber, @Mock Log log) {
        final String text = uniqueString();
        when(log.get()).thenReturn(text);
        when(subscriber.test(any())).thenReturn(true);
        final String id = uniqueString();
        
        runWithScenario(metalog -> {
            try (AutoClose closeSubscriber = metalog.subscribe(subscriber)) {
                final AutoClose ignored = closeSubscriber;
                
                metalog.publish(log, b -> b.id(id).block());
            }
            
            // subscription is closed and should no longer receive logs
            metalog.publish(log, b -> b.id(id).block());
            verify(subscriber, times(1)).receive(any(), metaWithId(id));
        });
    }
    
    @Test
    default void metalog_subscribe_close_Works(@Mock Subscriber subscriber, @Mock Log log) {
        final String text = uniqueString();
        when(log.get()).thenReturn(text);
        when(subscriber.test(any())).thenReturn(true);
        final String id = uniqueString();
        
        runWithScenario(metalog -> {
            try (AutoClose closeSubscriber = metalog.subscribe(subscriber)) {
                final AutoClose ignored = closeSubscriber;
                
                metalog.publish(log, b -> b.id(id).block());
            }
            
            // subscription is closed and should no longer receive logs
            metalog.publish(log, b -> b.id(id).block());
            verify(subscriber, times(1)).receive(any(), metaWithId(id));
        });
    }
    
    @Test
    default void metalog_InternalCoverage() {
        assertInstantiateThrows(MetalogTestsTools.class);
    }
    
    final class MetalogTestsTools {
        private MetalogTestsTools() {
            throw new AssertionError("Illegal constructor call.");
        }

        interface ScenarioConfig extends Consumer<Metalog>{
            default Metalog.Config getMetalogConfig() {
                return Metalog.Config.DEFAULT;
            }
        }
        
        static void runWithScenario(ScenarioConfig scenarioConfig) {
            final Metalog metalog = createMetalog(scenarioConfig.getMetalogConfig());
            try (AutoClose closeLogs = metalog.open()) {
                AutoClose ignoreWarning = closeLogs;
                scenarioConfig.accept(metalog);
            }
        }
    }
}

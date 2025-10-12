package io.github.jonloucks.metalog.test;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.Contract;
import io.github.jonloucks.contracts.api.Contracts;
import io.github.jonloucks.contracts.api.Promisor;
import io.github.jonloucks.metalog.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;

import java.time.Duration;
import java.util.function.Consumer;

import static io.github.jonloucks.contracts.test.Tools.*;
import static io.github.jonloucks.metalog.api.GlobalMetalog.createMetalog;
import static io.github.jonloucks.metalog.test.MetalogTests.MetalogTestsTools.runWithScenario;
import static io.github.jonloucks.metalog.test.Tools.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    default void metalog_WhenConsoleFails() {
        Tools.withContracts(cc -> {
            
            final Contracts contracts = new Contracts() {
                @Override
                public AutoClose open() {
                    return cc.open();
                }
                
                @Override
                public <T> T claim(Contract<T> contract) {
                    if (Console.CONTRACT == contract) {
                        throw new IllegalStateException("Test Error Injection.");
                    }
                    return cc.claim(contract);
                }
                
                @Override
                public <T> boolean isBound(Contract<T> contract) {
                    return cc.isBound(contract);
                }
                
                @Override
                public <T> AutoClose bind(Contract<T> contract, Promisor<T> promisor) {
                    return cc.bind(contract, promisor);
                }
            };
            
            final Metalog.Config config = new Metalog.Config() {
                @Override
                public Contracts contracts() {
                    return contracts;
                }
            };
            final Metalog metalog = createMetalog(config);
            final IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
                try (AutoClose closeMetalog = metalog.open()) {
                    final AutoClose ignored = closeMetalog;
                }
            });
            assertThrown(thrown, "Test Error Injection.");
        });
    }
    
    @Test
    default void metalog_CloseWhileBusy() {
        final Metalog.Config config = new Metalog.Config() {
            @Override
            public Duration shutdownTimeout() {
                return Duration.ofMillis(5);
            }
        };
        assertDoesNotThrow(() -> {
            final Metalog metalog = createMetalog(config);
            final Subscriber subscriber = (l, m) -> sleep(Duration.ofMillis(100));
            try (AutoClose closeMetalog = metalog.open();
                 AutoClose closeSubscriber = metalog.subscribe(subscriber)) {
                final AutoClose ignored = closeSubscriber;
                final int threadCount = Runtime.getRuntime().availableProcessors();
                final Thread[] threads = new Thread[threadCount];
                for (int threadIndex = 0; threadIndex < threadCount; threadIndex++) {
                    threads[threadIndex] = new Thread(() -> {
                        final String name = uniqueString();
                        for (int logIndex = 0; logIndex < 1_000; logIndex++) {
                            if (logIndex % 2 == 0) {
                                metalog.publish(() -> name, b -> b.key(name));
                            } else {
                                metalog.publish(() -> name, b -> {
                                });
                            }
                        }
                    });
                    threads[threadIndex].setDaemon(false);
                    threads[threadIndex].start();
                }
                sleep(Duration.ofMillis(20));
                implicitClose(closeMetalog);
            }
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

package io.github.jonloucks.metalog.test;

import io.github.jonloucks.contracts.api.*;
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
import java.util.function.Predicate;

import static io.github.jonloucks.contracts.test.Tools.*;
import static io.github.jonloucks.metalog.api.GlobalMetalog.createMetalog;
import static io.github.jonloucks.metalog.test.MetalogTests.MetalogTestsTools.runWithScenario;
import static io.github.jonloucks.metalog.test.Tools.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SuppressWarnings("ALL")
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
    default void metalog_addFilter_CloseTwice_DoesNotThrow() {
        runWithScenario(metalog -> {
            final Predicate<Meta> filter = m -> true;
            try (AutoClose removeFilter = metalog.addFilter(filter)) {
                implicitClose(removeFilter);
                assertDoesNotThrow(() -> implicitClose(removeFilter));
            }
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
        withContracts(cc -> {
            
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
                
                @Override
                public <T> AutoClose bind(Contract<T> contract, Promisor<T> promisor, BindStrategy bindStrategy) {
                    return cc.bind(contract, promisor, bindStrategy);
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
                //noinspection resource
                metalog.open();
            });
            assertThrown(thrown, "Test Error Injection.");
        });
    }
    
    @Test
    default void metalog_CloseWhileBusy() {
        withContracts(contracts -> {
            final Metalog.Config config = new Metalog.Config() {
                @Override
                public Contracts contracts() {
                    return contracts;
                }
                @Override
                public Duration shutdownTimeout() {
                    return Duration.ofSeconds(1);
                }
            };
            assertDoesNotThrow(() -> {
                final Metalog metalog = createMetalog(config);
                final Subscriber subscriber = (l, m) -> {
                    sleep(Duration.ofMillis(10));
                    return Outcome.CONSUMED;
                };
                final Subscriber skippedSubscriber = new Subscriber() {
                    @Override
                    public Outcome receive(Log log, Meta meta) {
                        return Outcome.SKIPPED;
                    }
                    
                    @Override
                    public boolean test(Meta meta) {
                        return false;
                    }
                };
                try (AutoClose closeMetalog = metalog.open();
                     AutoClose closeSubscriber = metalog.subscribe(subscriber);
                     AutoClose closeSkippedSubscriber = metalog.subscribe(skippedSubscriber)) {
                    final AutoClose ignored = closeSubscriber, ignored2 = closeSkippedSubscriber;
                    final int threadCount = 4;
                    final Thread[] threads = new Thread[threadCount];
                    for (int threadIndex = 0; threadIndex < threadCount; threadIndex++) {
                        threads[threadIndex] = new Thread(() -> {
                            final String name = uniqueString();
                            for (int logIndex = 0; logIndex < 100; logIndex++) {
                                if (logIndex % 2 == 0) {
                                    metalog.publish(() -> name, b -> b.key(name));
                                } else {
                                    metalog.publish(() -> name, b -> {});
                                }
                            }
                        });
                        threads[threadIndex].start();
                    }
                    sleep(Duration.ofMillis(100));
                    implicitClose(closeMetalog);
                }
            });
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
        }
        
        static void runWithScenario(ScenarioConfig scenarioConfig) {
            withMetalog((contracts,metalog) -> {
                scenarioConfig.accept(metalog);
            });
        }
    }
}

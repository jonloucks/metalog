package io.github.jonloucks.metalog.test;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.metalog.api.Metalog;
import io.github.jonloucks.metalog.api.Subscriber;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static io.github.jonloucks.contracts.test.Tools.sleep;
import static io.github.jonloucks.metalog.test.Tools.withMetalog;
import static io.github.jonloucks.metalog.test.TorrentTests.TorrentTestsTools.runWithScenario;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public interface TorrentTests {
    
    @Test
    default void torrent_keyed() {
        runWithScenario(new TorrentTestsTools.ScenarioConfig() {
            @Override
            public int chanceOfKeyed() {
                return 100;
            }
        });
    }
    
    @Test
    default void torrent_unkeyed() {
        runWithScenario(new TorrentTestsTools.ScenarioConfig() {
            @Override
            public int chanceOfKeyed() {
                return 0;
            }
        });
    }
    
    @Test
    default void torrent_half_and_half() {
        runWithScenario(new TorrentTestsTools.ScenarioConfig() {
        });
    }
    
    final class TorrentTestsTools {
        private TorrentTestsTools() {
            throw new AssertionError("Illegal constructor call.");
        }
        
        interface ScenarioConfig {
            default Metalog.Config getMetalogConfig() {
                return Metalog.Config.DEFAULT;
            }
            
            default int chanceOfKeyed() {
                return 50;
            }
        }
        
        static void runWithScenario(ScenarioConfig scenarioConfig) {
            withMetalog(scenarioConfig.getMetalogConfig(),(contracts, metalog) -> {
                final int producerThreads = 123;
                final int messagesPerThread = 321;
                final CountDownLatch messagesCompletedLatch = new CountDownLatch(producerThreads * messagesPerThread);
                final SequencerTracker sequencer = new SequencerTracker();
                final Random random = new Random();
                
                final Subscriber subscriber = (l, m) -> {
                    try {
                        sequencer.receive(l,m);
                    } finally {
                        messagesCompletedLatch.countDown();
                    }
                };
                
                final BooleanSupplier flipKeyedCoin = () -> scenarioConfig.chanceOfKeyed() != 0 && Math.abs(random.nextInt(100)) <= scenarioConfig.chanceOfKeyed();
                
                final Subscriber slowSubscriber = (l, m) -> sleep(Duration.ofMillis(5));
                
                try (AutoClose closeSubscription = metalog.subscribe(subscriber);
                     AutoClose closeSlowSubscriber = metalog.subscribe(slowSubscriber)) {
                    final AutoClose ignored = closeSubscription, ignored2 = closeSlowSubscriber;
                    
                    for (int i = 1; i <= producerThreads; i++) {
                        final String name = "Thread-" + i;
                        final SequencerTracker.Sequence sequence = sequencer.getSequence(name);
                        
                        final Thread thread = new Thread(() -> {
                            for (int j = 0; j < messagesPerThread; j++) {
                                if (flipKeyedCoin.getAsBoolean() ) {
                                    metalog.publish(() -> name, b -> b.value(sequence.incrementCounter()).key(sequence.key()));
                                } else {
                                    metalog.publish(() -> name, b -> {});
                                }
                            }
                        });
                        thread.setName(name);
                        thread.start();
                    }
                    
                    try {
                        if (!messagesCompletedLatch.await(5, TimeUnit.MINUTES)) {
                            fail("Torrent consumers took too long.");
                        }
                    } catch (InterruptedException e) {
                        fail("Torrent consumers took too long.");
                    }
                    
                    assertEquals(0, sequencer.getFailureCount(), "Failures");
                }
            });
        }
    }
}

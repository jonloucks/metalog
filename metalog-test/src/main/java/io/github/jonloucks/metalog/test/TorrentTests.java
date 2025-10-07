package io.github.jonloucks.metalog.test;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.metalog.api.Log;
import io.github.jonloucks.metalog.api.Meta;
import io.github.jonloucks.metalog.api.Metalog;
import io.github.jonloucks.metalog.api.Subscriber;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.github.jonloucks.contracts.test.Tools.sleep;
import static io.github.jonloucks.metalog.test.Tools.withMetalog;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public interface TorrentTests {
    
    @Test
    default void torrent_keyed() {
        final Metalog.Config config = new Metalog.Config(){};
        withMetalog(config,(contracts, metalog) -> {
            final int producerThreads = 12;
            final int messagesPerThread = 1_000;
            final CountDownLatch messagesCompletedLatch = new CountDownLatch(producerThreads * messagesPerThread);
            final SequencerTracker sequencer = new SequencerTracker();
    
            final Subscriber subscriber = (l, m) -> {
                try {
                    sequencer.receive(l,m);
                } finally {
                    messagesCompletedLatch.countDown();
                }
            };
            
            metalog.subscribe(new Subscriber() {
                @Override
                public boolean test(Meta meta) {
                    return true;
                }
                
                @Override
                public void receive(Log log, Meta meta) {
                    sleep(Duration.ofMillis(5));
                }
            });
            
            try (AutoClose closeSubscription = metalog.subscribe(subscriber)) {
                final AutoClose ignored = closeSubscription;
                
                for (int i = 1; i <= producerThreads; i++) {
                    final String name = "Thread-" + i;
                    final SequencerTracker.Sequence sequence = sequencer.getSequence(name);
                    
                    final Thread thread = new Thread(() -> {
                        for (int j = 0; j < messagesPerThread; j++) {
                            metalog.publish(() -> name, b -> b.value(sequence.incrementCounter()).key(sequence.key()));
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
    
    @Test
    default void torrent_unkeyed() {
        final Metalog.Config config = new Metalog.Config(){};
        withMetalog(config,(contracts, metalog) -> {
            final int producerThreads = 12;
            final int messagesPerThread = 1_000;
            final CountDownLatch messagesCompletedLatch = new CountDownLatch(producerThreads * messagesPerThread);
            
            final Subscriber subscriber = (l, m) -> {
                messagesCompletedLatch.countDown();
            };
            
            metalog.subscribe(new Subscriber() {
                @Override
                public boolean test(Meta meta) {
                    return true;
                }
                
                @Override
                public void receive(Log log, Meta meta) {
                    sleep(Duration.ofMillis(5));
                }
            });
            
            try (AutoClose closeSubscription = metalog.subscribe(subscriber)) {
                final AutoClose ignored = closeSubscription;
                
                for (int i = 1; i <= producerThreads; i++) {
                    final String name = "Thread-" + i;
          
                    final Thread thread = new Thread(() -> {
                        for (int j = 0; j < messagesPerThread; j++) {
                            metalog.publish(() -> name);
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
            }
        });
    }
}

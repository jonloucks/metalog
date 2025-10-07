package io.github.jonloucks.metalog.test;

import io.github.jonloucks.metalog.api.Log;
import io.github.jonloucks.metalog.api.Meta;
import io.github.jonloucks.metalog.api.Subscriber;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

final class SequencerTracker implements Subscriber {

    @Override
    public void receive(Log log, Meta meta) {
        final Optional<String> optionalKey = meta.getKey();
        // storing the sequence number directly in the meta value for tests.
        final Optional<Object> optionalValue = meta.getValue();
        if (optionalKey.isPresent() && optionalValue.isPresent()) {
            final Sequence sequence = getSequence(optionalKey.get());
            synchronized (sequence) {
                final int actual = (Integer) optionalValue.get();
                final int expected = sequence.expected();
                if (actual != expected) {
                    failures++;
                } else {
                    sequence.incrementExpected();
                }
            }
        }
    }
    
    SequencerTracker() {}
    
    Sequence getSequence(String key)  {
        return keySequences.computeIfAbsent(key, Sequence::new);
    }
    
    int getFailureCount() {
        return failures;
    }
    
    static class Sequence {
        String key() {
            return key;
        }
        synchronized int incrementCounter() {
            return ++counter;
        }
        synchronized void incrementExpected() {
            ++expected;
        }
        synchronized int expected() {
            return expected;
        }
        private Sequence(String key) {
            this.key = key;
        }
        private final String key;
        private int counter = 0;
        private int expected = 1;
    }
    
    private final ConcurrentHashMap<String, Sequence> keySequences = new ConcurrentHashMap<>();
    private int failures = 0;
}

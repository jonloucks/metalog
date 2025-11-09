package io.github.jonloucks.metalog.test;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.metalog.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static io.github.jonloucks.contracts.test.Tools.ignore;
import static io.github.jonloucks.metalog.api.Outcome.*;
import static io.github.jonloucks.metalog.test.Tools.withMetalog;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public interface KeyedSubscriptionTests {
    
    @Test
    default void keyedSubscription_SubscriberWithKey_WhenMetaHasDifferentKey_IsSkipped(@Mock Subscriber subscriber) {
        when(subscriber.test(any())).thenReturn(true);
        when(subscriber.getKey()).thenReturn(Optional.of("green"));
        withMetalog(b -> b.keyedSubscription(true), (contracts, metalog) -> {
            try (AutoClose closeSubscription = metalog.subscribe(subscriber)) {
                ignore(closeSubscription);
                final Outcome outcome = metalog.publish(() -> "x", b -> b.key("blue"));
                assertEquals(SKIPPED, outcome);
            }
        });
    }
    
    @Test
    default void keyedSubscription_SubscriberWithNoKey_WhenMetaHasNoKey_IsConsumed(@Mock Subscriber subscriber) {
        when(subscriber.test(any())).thenReturn(true);
        when(subscriber.getKey()).thenReturn(Optional.empty());
        when(subscriber.receive(any(),any())).thenReturn(Outcome.CONSUMED);
        withMetalog(b -> b.keyedSubscription(true), (contracts, metalog) -> {
            try (AutoClose closeSubscription = metalog.subscribe(subscriber)) {
                ignore(closeSubscription);
                final Outcome outcome = metalog.publish(() -> "x", b -> {});
                assertEquals(DISPATCHED, outcome);
            }
        });
    }
    
    @Test
    default void keyedSubscription_SubscriberWithNoKey_WhenMetaHasNoKey_ButFilterFails_IsSkipped(@Mock Subscriber subscriber) {
        when(subscriber.test(any())).thenReturn(true);
        when(subscriber.getKey()).thenReturn(Optional.empty());
        withMetalog(b -> b.keyedSubscription(true), (contracts, metalog) -> {
            try (AutoClose closeSubscription = metalog.subscribe(subscriber);
                 AutoClose closeFilter = metalog.addFilter(m -> false)) {
                ignore(closeSubscription); ignore(closeFilter);
                final Outcome outcome = metalog.publish(() -> "x", b -> {});
                assertEquals(SKIPPED, outcome);
            }
        });
    }
    
    @Test
    default void keyedSubscription_SubscriberWithNoKey_WhenMetaHasNoKey_ButTestFails_IsSkipped(@Mock Subscriber subscriber) {
        when(subscriber.test(any())).thenReturn(false);
        when(subscriber.getKey()).thenReturn(Optional.empty());
        withMetalog(b -> b.keyedSubscription(true), (contracts, metalog) -> {
            try (AutoClose closeSubscription = metalog.subscribe(subscriber)) {
                ignore(closeSubscription);
                final Outcome outcome = metalog.publish(() -> "x", b -> {});
                assertEquals(SKIPPED, outcome);
            }
        });
    }
    
    @Test
    default void keyedSubscription_SubscriberWithKey_WhenMetaHasSameKey_IsConsumed(@Mock Subscriber subscriber) {
        when(subscriber.test(any())).thenReturn(true);
        when(subscriber.getKey()).thenReturn(Optional.of("green"));
        when(subscriber.receive(any(),any())).thenReturn(Outcome.CONSUMED);
        withMetalog(b -> b.keyedSubscription(true), (contracts, metalog) -> {
            try (AutoClose closeSubscription = metalog.subscribe(subscriber)) {
                ignore(closeSubscription);
                final Outcome outcome = metalog.publish(() -> "x", b -> b.key("green"));
                assertEquals(DISPATCHED, outcome);
            }
        });
    }
    
    @Test
    default void keyedSubscription_SubscriberWithKey_WhenMetaHasSameKey_ButTestFails_IsSkipped(@Mock Subscriber subscriber) {
        when(subscriber.test(any())).thenReturn(false);
        when(subscriber.getKey()).thenReturn(Optional.of("green"));
        withMetalog(b -> b.keyedSubscription(true), (contracts, metalog) -> {
            try (AutoClose closeSubscription = metalog.subscribe(subscriber)) {
                ignore(closeSubscription);
                final Outcome outcome = metalog.publish(() -> "x", b -> b.key("green"));
                assertEquals(SKIPPED, outcome);
            }
        });
    }
    
    @Test
    default void keyedSubscription_SubscriberWithKey_WhenMetaHasNoKey_IsSkipped(@Mock Subscriber subscriber) {
        when(subscriber.test(any())).thenReturn(true);
        when(subscriber.getKey()).thenReturn(Optional.of("green"));
        withMetalog(b -> b.keyedSubscription(true), (contracts, metalog) -> {
            try (AutoClose closeSubscription = metalog.subscribe(subscriber)) {
                ignore(closeSubscription);
                final Outcome outcome = metalog.publish(() -> "x", b -> {});
                assertEquals(SKIPPED, outcome);
            }
        });
    }
    
    @Test
    default void keyedSubscription_SubscriberWithOutKey_WhenMetaHasKey_IsSkipped(@Mock Subscriber subscriber) {
        when(subscriber.test(any())).thenReturn(true);
        when(subscriber.getKey()).thenReturn(Optional.empty());
        withMetalog(b -> b.keyedSubscription(true), (contracts, metalog) -> {
            try (AutoClose closeSubscription = metalog.subscribe(subscriber)) {
                ignore(closeSubscription);
                final Outcome outcome = metalog.publish(() -> "x", b -> b.key("green"));
                assertEquals(SKIPPED, outcome);
            }
        });
    }
    
    @Test
    default void keyedSubscription_TwoSubscribersWithKey_WithOneSkipped_Works(@Mock Subscriber subscriber1,@Mock Subscriber subscriber2) {
        when(subscriber1.test(any())).thenReturn(true);
        when(subscriber1.getKey()).thenReturn(Optional.of("green"));
        when(subscriber1.receive(any(),any())).thenReturn(CONSUMED);
        
        when(subscriber2.test(any())).thenReturn(true);
        when(subscriber2.getKey()).thenReturn(Optional.of("green"));
        when(subscriber2.receive(any(),any())).thenReturn(SKIPPED);
        withMetalog(b -> b.keyedSubscription(true), (contracts, metalog) -> {
            try (AutoClose closeSubscription1 = metalog.subscribe(subscriber1);
                 AutoClose closeSubscription2 = metalog.subscribe(subscriber2)) {
                ignore(closeSubscription1); ignore(closeSubscription2);
                final Outcome outcome = metalog.publish(() -> "x", b -> b.key("green"));
                assertEquals(DISPATCHED, outcome);
            }
        });
    }
}

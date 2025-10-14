package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.contracts.api.AutoClose;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static io.github.jonloucks.metalog.impl.Internal.runWithIgnore;
import static java.lang.Boolean.*;

/**
 * Provides runtime validation
 */
public final class Stub {
    
    private Stub() {
    }
    
    /**
     * Provides runtime validation
     */
    public static void validate() {
        testAsserts();
        testIdempotentOpen();
        testIdempotentClose();
        testIdempotentFailedOpen();
        testIdempotentFailedClose();
    }
    
    private static void testAsserts() {
        assertFails(()-> assertFails(()-> {}));
        assertFails(()-> assertEquals(null, 5));
        assertFails(()-> assertEquals(5, null));
        assertFails(()-> assertEquals("green", "blue"));
        assertFails(()-> assertEquals(TRUE, FALSE));
    }
    
    private static void testIdempotentOpen() {
        final IdempotentImpl idempotent = new IdempotentImpl();
        assertEquals(TRUE, idempotent.isRejecting());
        assertEquals(FALSE, idempotent.isActive());
        assertEquals(FALSE, idempotent.transitionTo(IdempotentImpl.State.CREATED));
        assertEquals(AUTO_CLOSE, idempotent.transitionToOpened(() -> {
            assertEquals(TRUE, idempotent.isActive());
            assertEquals(FALSE, idempotent.isRejecting());
            assertEquals(FALSE, idempotent.transitionTo(IdempotentImpl.State.OPENING));
            return AUTO_CLOSE;
        }));
        assertEquals(FALSE, idempotent.transitionTo(IdempotentImpl.State.CREATED));
        assertEquals(TRUE, idempotent.isActive());
        assertEquals(FALSE, idempotent.isRejecting());
        assertEquals(AutoClose.NONE, idempotent.transitionToOpened(OPEN_BLOCK));
        AUTO_CLOSE.close();
    }
    
    private static void testIdempotentClose() {
        final IdempotentImpl idempotent = new IdempotentImpl();
        try (AutoClose close = idempotent.transitionToOpened(OPEN_BLOCK)) {
            ignore(close);
        }
        idempotent.transitionToClosed(RUNNABLE);
        assertEquals(FALSE, idempotent.isActive());
        assertEquals(TRUE, idempotent.isRejecting());
        
        idempotent.transitionToClosed(RUNNABLE);
        assertEquals(FALSE, idempotent.isActive());
        assertEquals(TRUE, idempotent.isRejecting());
    }
    
    private static void testIdempotentFailedOpen() {
        final IdempotentImpl idempotent = new IdempotentImpl();
        runWithIgnore(() -> ignore(idempotent.transitionToOpened(() -> {
            throw new IllegalStateException("Oh my.");
        })));
        assertEquals(FALSE, idempotent.isActive());
        assertEquals(TRUE, idempotent.isRejecting());
    }
    
    private static void testIdempotentFailedClose() {
        final IdempotentImpl idempotent = new IdempotentImpl();
        try (AutoClose close = idempotent.transitionToOpened(OPEN_BLOCK)){
            ignore(close);
            runWithIgnore(() -> {
                idempotent.transitionToClosed(() -> {
                    assertEquals(FALSE, idempotent.transitionTo(IdempotentImpl.State.OPENING));
                    throw new IllegalStateException("Oh my.");
                });
            });

        }
        assertEquals(FALSE, idempotent.isActive());
        assertEquals(TRUE, idempotent.isRejecting());
    }
    
    private static void assertEquals(Object expected, Object actual) {
        if (expected == actual) {
            return;
        }
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError("Failed runtime check.");
        }
    }
    
    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws AssertionError;
    }
    
    private static void assertFails(ThrowingRunnable runnable) {
        final AtomicReference<AssertionError> throwable = new AtomicReference<>();
        try {
            runnable.run();
           throwable.set(new AssertionError("Failed runtime check."));
        } catch (AssertionError thrown) {
            ignore(thrown);
        }
        if (throwable.get() != null) {
            throw throwable.get();
        }
    }
    
   @SuppressWarnings("unused")
   private static void ignore(Object ignored) {
   }
    
    private static final Runnable RUNNABLE = () -> {};
    private static final AutoClose AUTO_CLOSE = () -> {};
    private static final Supplier<AutoClose> OPEN_BLOCK = ()-> AUTO_CLOSE;
}

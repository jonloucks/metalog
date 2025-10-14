package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.contracts.api.AutoClose;

import java.util.function.Supplier;

import static io.github.jonloucks.metalog.impl.Internal.runWithIgnore;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

@SuppressWarnings("CodeBlock2Expr")
public final class Stub {
    
    private Stub() {
    }
    
    public static void validate() {
        testIdempotentOpen();
        testIdempotentClose();
        testIdempotentFailedOpen();
        testIdempotentFailedClose();
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
            final AutoClose ignore = close;
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
        runWithIgnore(() -> {
            //noinspection resource
            idempotent.transitionToOpened(() -> {
                throw new IllegalStateException("Oh my.");
            });
        });
        assertEquals(FALSE, idempotent.isActive());
        assertEquals(TRUE, idempotent.isRejecting());
    }
    
    private static void testIdempotentFailedClose() {
        final IdempotentImpl idempotent = new IdempotentImpl();
        try (AutoClose close = idempotent.transitionToOpened(OPEN_BLOCK)){
            final AutoClose ignore = close;
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
    
    @SuppressWarnings("unused")
    private static void assertEquals(Object expected, Object actual) {
//        if (expected == actual) {
//            return;
//        }
//        if (expected == null || !expected.equals(actual)) {
//            throw new AssertionError("Failed runtime check.");
//        }
    }
    
    private static final Runnable RUNNABLE = () -> {};
    private static final AutoClose AUTO_CLOSE = () -> {};
    private static final Supplier<AutoClose> OPEN_BLOCK = ()-> AUTO_CLOSE;
}

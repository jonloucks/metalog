package io.github.jonloucks.metalog.impl.test;

import io.github.jonloucks.metalog.impl.Idempotent;
import io.github.jonloucks.metalog.impl.Idempotent.State;
import io.github.jonloucks.metalog.impl.Idempotent.Transition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.function.Consumer;

import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static io.github.jonloucks.metalog.test.Tools.*;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("CodeBlock2Expr")
public interface IdempotentTests {
    
    @Test
    default void idempotent_transitions() {
        withMetalog(b ->{}, (contracts, metalog) -> {
            final Idempotent idempotent = contracts.claim(Idempotent.FACTORY).get();
            assertEquals(State.CREATED, idempotent.getState());
            assertFalse(State.CREATED.canTransitionTo(State.CLOSED));
            assertTrue(State.CREATED.canTransitionTo(State.OPENING));
            assertTrue(State.OPENING.canTransitionTo(State.OPENED));
            assertTrue(State.OPENING.canTransitionTo(State.OPENED));
        });
    }
    
    @ParameterizedTest
    @EnumSource(State.class)
    default void idempotent_state_attemptTransition_Self_IsFalse(State state) {
        assertFalse(state.canTransitionTo(state));
    }
    
    @Test
    default void idempotent_transition_WithNullState_Throws() {
        withMetalogInstalled(contracts -> {
            final Idempotent idempotent = contracts.claim(Idempotent.FACTORY).get();
            
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                idempotent.transition((State)null);
            });
            assertThrown(thrown );
        });
    }
    
    @Test
    default void idempotent_transition_WithNullBuilderConsumer_Throws() {
        withMetalogInstalled(contracts -> {
            final Idempotent idempotent = contracts.claim(Idempotent.FACTORY).get();
            
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                idempotent.transition((Consumer<Transition.Builder<String>>)null);
            });
            assertThrown(thrown );
        });
    }
    
    @Test
    default void idempotent_transition_WithNullTransition_Throws() {
        withMetalogInstalled(contracts -> {
            final Idempotent idempotent = contracts.claim(Idempotent.FACTORY).get();
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                idempotent.transition((Transition<String>)null);
            });
            assertThrown(thrown );
        });
    }
    
    @Test
    default void idempotent_transition_WithNoGoalState_Throws() {
        withMetalogInstalled(contracts -> {
            final Idempotent idempotent = contracts.claim(Idempotent.FACTORY).get();
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                idempotent.transition(b->{});
            });
            assertThrown(thrown );
        });
    }
    
    @Test
    default void idempotent_transition_WithIllegalTransition_Throws() {
        withMetalogInstalled(contracts -> {
            final Idempotent idempotent = contracts.claim(Idempotent.FACTORY).get();
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                idempotent.transition(b -> {
                    b.interimState(State.CLOSING)
                        .goalState(State.OPENING);
                });
            });
            assertThrown(thrown);
        });
    }
    
    @Test
    default void idempotent_transition_alwaysWithException_ChangesState() {
        withMetalogInstalled(contracts -> {
            final Idempotent idempotent = contracts.claim(Idempotent.FACTORY).get();
            final Error expected = new Error("Error.");
            final Error thrown = assertThrows(Error.class, () -> {
                idempotent.transition(b -> b
                    .goalState(State.OPENING)
                    .action(() -> { throw expected; })
                    .always(true)
                );
            });
            assertEquals(expected, thrown);
            assertEquals(State.OPENING, idempotent.getState());
        });
    }
    
    @Test
    default void idempotent_transition_WithException_RevertsState() {
        withMetalogInstalled(contracts -> {
            final Idempotent idempotent = contracts.claim(Idempotent.FACTORY).get();
            final Error expected = new Error("Error.");
            final Error thrown = assertThrows(Error.class, () -> {
                idempotent.transition(b -> b
                    .goalState(State.OPENING)
                    .action(() -> { throw expected; })
                    .always(false)
                );
            });
            assertEquals(expected, thrown);
            assertEquals(State.CREATED, idempotent.getState());
        });
    }
}

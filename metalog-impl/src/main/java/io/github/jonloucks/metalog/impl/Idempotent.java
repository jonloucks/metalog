package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.Contract;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.github.jonloucks.metalog.impl.Idempotent.State.*;

/**
 * Note: This an implementation specific interface. A different implementation of Metalog will not provide it
 * Is exposed for advanced logging and testing
 */
public interface Idempotent {
    
    /**
     * The factory contract for creating a new Idempotent
     */
    Contract<Supplier<Idempotent>> FACTORY = Contract.create("Idempotent Factory");
    
    State getState();
    
    boolean transition(State state);
    
    <T> T transition(Consumer<Transition.Builder<T>> builder);
    
    <T> T transition(Transition<T> transition);
    
    default boolean isRejecting() {
        return getState().isRejecting();
    }

    default void transitionToClosed(Runnable action) {
        transition(b -> b
            .interimState(CLOSING)
            .goalState(CLOSED)
            .always(true)
            .action(action)
        );
    }
    
    default AutoClose transitionToOpened(Supplier<AutoClose> opener) {
        return transition(b -> b
            .interimState(OPENING)
            .goalState(OPENED)
            .action(opener)
            .orElse(() -> AutoClose.NONE)
        );
    }
   
    enum State {
        CREATED {
            @Override
            public boolean canTransitionTo(State state) {
                return OPENING == state;
            }
        },
        OPENING {
            @Override
            public boolean canTransitionTo(State state) {
                return OPENED == state || CREATED == state;
            }
            @Override
            public boolean isRejecting() {
                return false;
            }
        },
        OPENED() {
            @Override
            public boolean canTransitionTo(State state) {
                return CLOSING == state;
            }
            @Override
            public boolean isRejecting() {
                return false;
            }
        },
        CLOSING {
            @Override
            public boolean canTransitionTo(State state) {
                return CLOSED == state;
            }
            @Override
            public boolean isRejecting() {
                return false;
            }
        },
        CLOSED;
        
        public boolean isRejecting() {
            return true;
        }
        
        public boolean canTransitionTo(State state) {
            return false;
        }
        
        State() {
        }
    }
    
    interface Transition<T> {
        Optional<State> interimState();
        Optional<State> goalState();
        Optional<Supplier<T>> action();
        Optional<Supplier<T>> orElse();
        boolean always();
        
        interface Builder<T>  extends Transition<T>{
            Builder<T> interimState(State stepState);
            Builder<T> goalState(State goalState);
            Builder<T> action(Supplier<T> action);
            Builder<T> action(Runnable action);
            Builder<T> orElse(Supplier<T> orElse);
            Builder<T> always(boolean always);
        }
    }
}

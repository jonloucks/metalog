package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.contracts.api.AutoClose;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

final class IdempotentImpl {
    IdempotentImpl() {
    }
    
    AutoClose transitionToOpened(Supplier<AutoClose> opener) {
        if (transitionTo(State.OPENING)) {
            try {
                final AutoClose autoClose = opener.get();
                transitionTo(State.OPENED);
                return autoClose;
            } catch (Exception thrown) {
                transitionTo(State.CLOSED);
                throw thrown;
            }
        } else {
            return AutoClose.NONE;
        }
    }
    
    void transitionToClosed(Runnable closer) {
        if (transitionTo(State.CLOSING)) {
            try {
                closer.run();
            } finally {
                transitionTo(State.CLOSED);
            }
        }
    }
    
    boolean isActive() {
        return stateReference.get().isActive();
    }
    
    boolean isRejecting() {
        return !isActive();
    }

    boolean transitionTo(State state) {
        final State stateNow = stateReference.get();
        if (stateNow.canTransitionTo(state)) {
            return stateReference.compareAndSet(stateNow, state);
        }
        return false;
    }
  
    private final AtomicReference<State> stateReference = new AtomicReference<>(State.CREATED);
    

    
    enum State {
        CREATED {
            @Override
            boolean canTransitionTo(State state) {
                return OPENING == state;
            }
        },
        OPENING {
            @Override
            boolean canTransitionTo(State state) {
                return OPENED == state || CLOSED == state;
            }
            @Override
            boolean isActive() {
                return true;
            }
        },
        OPENED() {
            @Override
            boolean canTransitionTo(State state) {
                return CLOSING == state;
            }
            @Override
            boolean isActive() {
                return true;
            }
        },
        CLOSING {
            @Override
            boolean canTransitionTo(State state) {
                return CLOSED == state;
            }
            @Override
            boolean isActive() {
                return true;
            }
        },
        CLOSED;
     
         boolean isActive() {
             return false;
         }
         boolean canTransitionTo(State state) {
             return false;
         }
        
        State() {
        }
    }
}

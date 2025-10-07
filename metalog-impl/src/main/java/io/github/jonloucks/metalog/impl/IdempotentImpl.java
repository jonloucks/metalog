package io.github.jonloucks.metalog.impl;

import java.util.concurrent.atomic.AtomicReference;

final class IdempotentImpl {
    IdempotentImpl() {
    }
    
    boolean transitionToOpening() {
        return transitionTo(State.OPENING);
    }

    boolean transitionToOpened() {
        return transitionTo(State.OPENED);
    }
    
    boolean transitionToClosing() {
        return transitionTo(State.CLOSING);
    }
    
    boolean transitionToClosed() {
        return transitionTo(State.CLOSED);
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
    
    private enum State {
        CREATED {
            @Override
            boolean canTransitionTo(State state) {
                return state == OPENED || state == OPENING;
            }
            @Override
            boolean isActive() {
                return false;
            }
        },
        OPENING {
            @Override
            boolean canTransitionTo(State state) {
                return state == OPENED;
            }
            @Override
            boolean isActive() {
                return true;
            }
        },
        OPENED() {
            @Override
            boolean canTransitionTo(State state) {
                return CLOSING == state || CLOSED == state;
            }
            @Override
            boolean isActive() {
                return true;
            }
        },
        CLOSING {
            @Override
            boolean canTransitionTo(State state) {
                return state == CLOSED;
            }
            @Override
            boolean isActive() {
                return true;
            }
        },
        CLOSED {
            @Override
            boolean canTransitionTo(State state) {
                return false;
            }
            @Override
            boolean isActive() {
                return false;
            }
        };
     
        abstract boolean isActive();
        abstract boolean canTransitionTo(State state);
        
        State() {
        }
    }
}

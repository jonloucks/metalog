package io.github.jonloucks.metalog.impl;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.github.jonloucks.contracts.api.Checks.builderConsumerCheck;
import static io.github.jonloucks.contracts.api.Checks.nullCheck;

final class IdempotentImpl implements Idempotent {
    
    @Override
    public State getState() {
        return stateReference.get();
    }
    
    @Override
    public boolean transition(State goalState) {
        final State validGoalState = stateCheck(goalState);
        final State currentState = getState();
        if (currentState.canTransitionTo(validGoalState)) {
            return stateReference.compareAndSet(currentState, validGoalState);
        }
        return false;
    }
    
    @Override
    public <T> T transition(Consumer<Transition.Builder<T>> builderConsumer) {
        final Idempotent.Transition.Builder<T> transition = new IdempotentTransitionImpl<>();
        builderConsumerCheck(builderConsumer).accept(transition);
        return transition(transition);
    }
    
    @Override
    public <T> T transition(Transition<T> transition) {
        final Transition<T> validTransition = nullCheck(transition, "Transition must be present.");
        final State goalState = getGoalState(validTransition);
        final State savedState = getState();
        
        if (interimTransition(validTransition, goalState)) {
            boolean exceptionThrown = false;
            try {
                return validTransition.action().map(Supplier::get).orElse(null);
            } catch (Throwable thrown) {
                exceptionThrown = true;
                throw thrown;
            } finally {
                if (validTransition.always() || !exceptionThrown) {
                    transition(goalState);
                } else {
                    transition(savedState);
                }
            }
        } else {
            return validTransition.orElse().map(Supplier::get).orElse(null);
        }
    }
    
    IdempotentImpl() {
    }
    
    private static <T> State getGoalState(Transition<T> validTransition) {
        return validTransition.goalState().orElseThrow(IdempotentImpl::getGoalStateNotPresentException);
    }
    
    private static State stateCheck(State state) {
        return nullCheck(state, "State must be present.");
    }
    
    private boolean interimTransition(Transition<?> transition, State goalState) {
        if (transition.interimState().isPresent()) {
            final State stepState = transition.interimState().get();
            if (stepState.canTransitionTo(goalState)) {
                return transition(stepState);
            }
            throw newStateChangeNotAllowed(stepState, goalState);
        } else {
            return true;
        }
    }
    
    private static IllegalArgumentException getGoalStateNotPresentException() {
        return new IllegalArgumentException("Transition goal state must be present.");
    }
    
    private static IllegalArgumentException newStateChangeNotAllowed(State stepState, State goalState) {
        return new IllegalArgumentException("Idempotent state change not allowed between " + stepState + " and " + goalState + ".");
    }
    
    private final AtomicReference<State> stateReference = new AtomicReference<>(State.CREATED);
}

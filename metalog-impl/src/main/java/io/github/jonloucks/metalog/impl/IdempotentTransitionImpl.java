package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.metalog.impl.Idempotent.State;

import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;

final class IdempotentTransitionImpl<T> implements Idempotent.Transition.Builder<T> {

    @Override
    public Builder<T> interimState(State interimState) {
        this.interimState = interimState;
        return this;
    }
    
    @Override
    public Builder<T> goalState(State goalState) {
        this.goalState = goalState;
        return this;
    }
    
    @Override
    public Builder<T> action(Supplier<T> action) {
        this.action = action;
        return this;
    }
    
    @Override
    public Builder<T> action(Runnable action) {
        if (null == action) {
            this.action = null;
        }  else {
            this.action = () -> {
                action.run();
                return null;
            };
        }
        return this;
    }
    
    @Override
    public Builder<T> orElse(Supplier<T> orElse) {
        this.orElse = orElse;
        return this;
    }
    
    @Override
    public Builder<T> always(boolean always) {
        this.always = always;
        return this;
    }
    
    @Override
    public Optional<State> interimState() {
        return ofNullable(interimState);
    }
    
    @Override
    public Optional<State> goalState() {
        return ofNullable(goalState);
    }
    
    @Override
    public Optional<Supplier<T>> action() {
        return ofNullable(action);
    }
    
    @Override
    public Optional<Supplier<T>> orElse() {
        return ofNullable(orElse);
    }
    
    @Override
    public boolean always() {
        return this.always;
    }
    
    IdempotentTransitionImpl() {
    
    }
    
    private State goalState;
    private boolean always;
    private State interimState;
    private Supplier<T> action;
    private Supplier<T> orElse;
}

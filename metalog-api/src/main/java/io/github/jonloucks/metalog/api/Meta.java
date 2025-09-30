package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.Contract;

import java.time.temporal.Temporal;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Optional.empty;

public interface Meta extends Entity {
    Meta DEFAULT = () -> "";
    
    default String getChannel() {
        return "info";
    }
    
    default Optional<String> getSequenceKey() {
        return empty();
    }
    
    default boolean isBlocking() {
        return false;
    }
    
    default Optional<Temporal> getTimestamp() {
        return empty();
    }
    
    default Optional<Throwable> getThrown() {
        return empty();
    }
    
    default Optional<Thread> getThread() {
        return empty();
    }
    
    interface Builder<B extends Builder<B> & Entity.Builder<B>> extends Meta, Entity.Builder<B> {
        Contract<Supplier<Meta.Builder<?>>> FACTORY_CONTRACT = Contract.create("Meta Builder Factory");
        
        B name(String name);

        B id(String id);
        
        B channel(String channel);
        
        B sequenceKey(String sequenceKey);
        
        B blocking(boolean temporal);
        
        B thrown(Throwable thrown);
        
        B timestamp(Temporal timestamp);
  
        B thread(Thread thread);
        
        B copy(Meta meta);
    }
}

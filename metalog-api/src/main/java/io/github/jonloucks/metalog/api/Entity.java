package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.Contract;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Optional.empty;

@FunctionalInterface
public interface Entity extends Supplier<CharSequence> {
    
    default Optional<String> getName() {
        return empty();
    }
    
    default Optional<String> getId() {
        return empty();
    }

    default Optional<Object> getValue() {
        return empty();
    }
    
    default Optional<Entities> getCorrelations() {
        return empty();
    }
    
    interface Builder<B extends Builder<B>> extends Entity {
        Contract<Supplier<Entity.Builder<?>>> FACTORY_CONTRACT = Contract.create("Entity Builder Factory");
        
        B id(String id);
        
        B name(String name);
        
        B text(Supplier<CharSequence> textSupplier);
        
        B value(Object value);
        
        B correlation(Consumer<Entity.Builder<?>> builder);
        
        B correlation(Entity entity);
        
        B correlations(Consumer<Entities.Builder<?>> builder);
        
        B copy(Entity fromEntity);
    }
}

package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.Contract;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Entities {
    
    @FunctionalInterface
    interface Visitor<T> {
        boolean visit(T t);
    }
    
    void visitEach(Visitor<? super Entity> visitor);
    
    Optional<Entity> findFirstIf(Predicate<? super Entity> filter);
    
    <T> Optional<T> findFirstWithType(Predicate<? super Entity> filter, Class<T> type);

    List<Entity> findAllIf(Predicate<? super Entity> filter);

    <T> List<T> findAllByNameWithType(String name, Class<T> type);
    
    <T> List<T> findAllWithType(Predicate<? super Entity> filter, Class<T> type);
    
    <T> Optional<T> findFirstByNameWithType(String name, Class<T> type);
    
    List<Entity> asList();
    
    boolean isEmpty();
    
    int size();
    
    interface Builder<B extends Builder<B>> extends Entities {
        Contract<Supplier<Entities.Builder<?>>> FACTORY_CONTRACT = Contract.create("Entities Builder Factory");
        
        boolean replaceIf(Predicate<? super Entity> filter, Entity entity);
        
        boolean removeIf(Predicate<? super Entity> filter);
        
        B unique(boolean unique);
        
        B entity(Consumer<Entity.Builder<?>> builderConsumer);
        
        B entity(Entity entity);
    }
}

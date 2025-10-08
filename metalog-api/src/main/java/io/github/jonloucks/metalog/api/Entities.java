package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.Contract;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Contains zero or more Entity instances
 * Supports both unique and duplicate entities, determined by the entity name and unique fields.
 * Examples of unique entities are "thread", "time", and "thrown"
 * Examples of non-unique entities could be "event" where there are multiple events associated with a log
 */
public interface Entities {
    
    /**
     * Visit each entity
     * @param visitor the callback to receive each entity
     */
    void visitEach(Visitor<? super Entity> visitor);
    
    /**
     * Finds the first entity matching a filter
     * @param filter determines the match
     * @return optionally the entity found
     */
    Optional<Entity> findFirstIf(Predicate<? super Entity> filter);
    
    /**
     * Finds the value of the first entity matching a filter and has the correct type
     * @param filter determines the match
     * @param type the type of value required
     * @return optionally the value found (null is a valid value)
     * @param <T> the type of value
     */
    <T> Optional<T> findFirstValueWithType(Predicate<? super Entity> filter, Class<T> type);
    
    /**
     * Finds all entities matching a filter
     * @param filter determines the match
     * @return a list of all matching entities
     */
    List<Entity> findAllIf(Predicate<? super Entity> filter);
    
    /**
     * Finds all the values of each entity matching a filter and has the correct type
     * @param filter determines the match
     * @param type the type of value required
     * @return a list of all the values (null is valid value)
     * @param <T> the type of value
     */
    <T> List<T> findAllValuesWithType(Predicate<? super Entity> filter, Class<T> type);
    
    /**
     * Create a new List containing all the entities in the proper order
     * @return the list
     */
    List<Entity> asList();
    
    /**
     * @return true when empty
     */
    boolean isEmpty();
    
    /**
     * @return the number of entities
     */
    int size();
    
    /**
     * The Entities Builder
     * @param <B> recursive builder type
     */
    interface Builder<B extends Builder<B>> extends Entities {
        
        /**
         * Entities Builder Factory Contract
         */
        Contract<Supplier<Entities.Builder<?>>> FACTORY_CONTRACT = Contract.create("Entities Builder Factory");
        
        /**
         * Replaces each entity that matches a filter with a replacement entity
         * @param filter determines if an entity will be replaced
         * @param replacement the entity used to replace any instances that match the filter
         * @return this builder
         */
        boolean replaceIf(Predicate<? super Entity> filter, Entity replacement);
        
        /**
         * Removes each entity that matches a filter
         * @param filter determines if an entity will be removed
         * @return this builder
         */
        boolean removeIf(Predicate<? super Entity> filter);

        /**
         * Add an entity via a builder consumer
         * @param builderConsumer the entity builder consumer
         * @return this builder
         */
        B entity(Consumer<Entity.Builder<?>> builderConsumer);
        
        /**
         * Add an entity
         * @param entity the entity to add
         * @return this builder
         */
        B entity(Entity entity);
    }
}

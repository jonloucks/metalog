package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.Contract;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Optional.empty;

/**
 * A generalized representation of anything
 */
@FunctionalInterface
public interface Entity extends Supplier<CharSequence> {
    
    /**
     * The optional user defined name for this entity
     * Examples: "myModule", "thread", "time", "thrown"
     * @return the optional name
     */
    default Optional<String> getName() {
        return empty();
    }
    
    /**
     * The optional user defined id for this entity
     * Examples: thread id, tenant id, etc
     * @return the optional id
     */
    default Optional<String> getId() {
        return empty();
    }
    
    /**
     * The optional user defined value for this entity
     * Examples: the thrown exception
     * @return the optional value
     */
    default Optional<Object> getValue() {
        return empty();
    }
    
    /**
     * The optional correlations for this entity
     * Examples: An entity representing the support contact information for error
     * @return the optional correlations
     */
    default Optional<Entities> getCorrelations() {
        return empty();
    }
    
    /**
     * @return if true then only one unique entity with the same name should exist
     */
    default boolean isUnique() {
        return false;
    }
    
    /**
     * Responsible for providing an easy way to build a meta instance used when logging
     * @param <B> the builder type
     */
    interface Builder<B extends Builder<B>> extends Entity {
        
        /**
         * Used to promise and claim the Entity.Builder implementation
         */
        Contract<Supplier<Entity.Builder<?>>> FACTORY_CONTRACT = Contract.create("Entity Builder Factory");
        
        /**
         * Set the id for the Entity
         * @param id the id for the Entity
         * @return this builder
         */
        B id(String id);
        
        /**
         * Set the name for the Entity
         * @param name the name for the Entity
         * @return this builder
         */
        B name(String name);
        
        /**
         * Set unique name for the Entity
         * @param unique true makes this entity unique
         * @return this builder
         */
        B unique(boolean unique);
        
        /**
         * Set the text supplier for the Entity
         * @param textSupplier the text supplier for the Entity
         * @return this builder
         */
        B text(Supplier<CharSequence> textSupplier);
        
        /**
         * Set the value for the Entity
         * @param value the value for the Entity
         * @return this builder
         */
        B value(Object value);
        
        /**
         * Add a correlation Entity
         * @param builderConsumer callback receiving the Entity.Builder for the new entity
         * @return this builder
         */
        B correlation(Consumer<Entity.Builder<?>> builderConsumer);
        
        /**
         * Add a correlation Entity
         * @param entity the entity to added as a correlation
         * @return this builder
         */
        B correlation(Entity entity);
        
        /**
         * Set correlations via created Entities.Builder
         * @param builderConsumer callback receiving the Entities.Builder for the new entities
         * @return this builder
         */
        B correlations(Consumer<Entities.Builder<?>> builderConsumer);
        
        /**
         * Copies non-empty values from an existing Entity
         * @param entity the Entity to copy
         * @return this builder
         */
        B copy(Entity entity);
    }
}

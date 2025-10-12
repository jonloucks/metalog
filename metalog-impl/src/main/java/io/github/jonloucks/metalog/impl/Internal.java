package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.metalog.api.*;

import java.util.function.Predicate;

import static io.github.jonloucks.contracts.api.Checks.nameCheck;
import static io.github.jonloucks.contracts.api.Checks.nullCheck;

final class Internal {
    
    /**
     * Utility class instantiation protection
     * Test coverage not possible, java module protections in place
     */
    private Internal() {
        // conflicting standards.  100% code coverage vs throwing exception on instantiation of utility class.
        // Java modules protects agents invoking private methods.
        // There are unit tests that will fail if this constructor is not private
    }

    static <T extends Log> T logCheck(T log) {
        return nullCheck(log, "Log must be present.");
    }
    
    static <T extends Meta> T metaCheck(T meta) {
        return nullCheck(meta, "Meta must be present.");
    }
    
    static Entity entityCheck(Entity entity) {
        return nullCheck(entity, "Entity must be present.");
    }
    
    static <T> Predicate<T> filterCheck(Predicate<T> filter) {
        return nullCheck(filter, "Filter must be present.");
    }
   
    static String channelCheck(String name) {
        return nullCheck(name, "Channel must be present.");
    }
    
    static <T> Visitor<T> visitorCheck(Visitor<T> visitor) {
        return nullCheck(visitor, "Visitor must be present.");
    }
    
    static <T> T commandCheck(T command) {
        return nullCheck(command, "Command must be present.");
    }
    
    static Subscriber subscriberCheck(Subscriber subscriber) {
        return nullCheck(subscriber, "Subscribers must be present.");
    }
    
    static Predicate<Entity> byName(String name) {
        final String validName = nameCheck(name);
        
        return entity -> entity.getName().filter(s -> validName.equals(s)).isPresent();
    }
    
    static Predicate<Entity> byUnique() {
        return Entity::isUnique;
    }
}

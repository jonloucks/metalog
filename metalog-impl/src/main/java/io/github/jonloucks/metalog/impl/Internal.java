package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.metalog.api.Entities;
import io.github.jonloucks.metalog.api.Entity;
import io.github.jonloucks.metalog.api.Log;
import io.github.jonloucks.metalog.api.Meta;

import java.util.Optional;
import java.util.function.Predicate;

import static io.github.jonloucks.contracts.api.Checks.nullCheck;

final class Internal {
    private Internal() {
        throw new AssertionError("Illegal constructor");
    }
    
    static <T extends Log> T logCheck(T log) {
        return nullCheck(log, "log was null");
    }
    
    static <T extends Meta> T metaCheck(T meta) {
        return nullCheck(meta, "meta was null");
    }
    
    static <T> T configCheck(T config) {
        return nullCheck(config, "config was null");
    }
    
    static <T> T builderCheck(T builder) {
        return nullCheck(builder, "builder was null");
    }
    
    static <T> Class<T> typeCheck(Class<T> type) {
        return nullCheck(type, "type was null");
    }
    
    static Entity entityCheck(Entity entity) {
        return nullCheck(entity, "entity was null");
    }
    
    static <T> Predicate<T> filterCheck(Predicate<T> filter) {
        return nullCheck(filter, "filter was null");
    }
    
    static <T> String nameCheck(String name) {
        return nullCheck(name, "name was null");
    }
    
    static <T> String channelCheck(String name) {
        return nullCheck(name, "channel was null");
    }
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static <T> boolean optionalEquals(Optional<T> a, Optional<T> b) {
        if (a == b) {
            return true;
        }
        if (null == a || null == b) {
            return false;
        }
        return a.isPresent() && b.isPresent() && a.get().equals(b.get());
    }
    
    static <T> Optional<T> findFirstByNameAndType(Entity entity, String name, Class<T> type) {
        final Optional<Entities> optional = entity.getCorrelations();
        if (optional.isPresent()) {
            return optional.get().findFirstByNameWithType(name, type);
        }
        return Optional.empty();
    }
    
    static Predicate<? super Entity> byName(String name) {
        final String validName = nameCheck(name);
        
        return entity -> {
            final Optional<String> optionalName = entity.getName();
            return optionalName.filter(s -> validName.equals(s)).isPresent();
        };
    }

}

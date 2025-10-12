package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.metalog.api.Entities;
import io.github.jonloucks.metalog.api.Entity;
import io.github.jonloucks.metalog.api.Visitor;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.github.jonloucks.contracts.api.Checks.*;
import static io.github.jonloucks.metalog.impl.Internal.*;

final class EntitiesImpl implements Entities.Builder<EntitiesImpl> {

    @Override
    public void visitEach(Visitor<? super Entity> visitor) {
        final Visitor<? super Entity> validVisitor = visitorCheck(visitor);
        for (Entity entity : entityList) {
            if (!validVisitor.visit(entity)) {
                return;
            }
        }
    }

    @Override
    public Optional<Entity> findFirstIf(Predicate<? super Entity> filter) {
        return entityList.stream().filter(filterCheck(filter)).findFirst();
    }

    @Override
    public List<Entity> findAllIf(Predicate<? super Entity> filter) {
        return entityList.stream().filter(filterCheck(filter)).collect(Collectors.toList());
    }
    
    @Override
    public boolean replaceIf(Predicate<? super Entity> filter, Entity replacement) {
        final Predicate<? super Entity> validFilter = filterCheck(filter);
        final Entity validEntity = entityCheck(replacement);
        int replacedEntities = 0;
        
        final ListIterator<Entity> iterator = entityList.listIterator();
        while (iterator.hasNext()) {
            final Entity nextEntity = iterator.next();
            if (validFilter.test(nextEntity)) {
                iterator.set(validEntity);
                ++replacedEntities;
            }
        }
        return replacedEntities > 0;
    }
    
    @Override
    public boolean removeIf(Predicate<? super Entity> filter) {
        return entityList.removeIf(filterCheck(filter));
    }
    
    @Override
    public boolean isEmpty() {
        return entityList.isEmpty();
    }
    
    @Override
    public int size() {
        return entityList.size();
    }

    @Override
    public EntitiesImpl entity(Consumer<Entity.Builder<?>> builderConsumer) {
        final EntityImpl builder = new EntityImpl();
        builderConsumerCheck(builderConsumer).accept(builder);
        entity(builder);
        return this;
    }
    
    @Override
    public EntitiesImpl entity(Entity entity) {
        final Entity validEntity = entityCheck(entity);
        if (validEntity.isUnique()) {
            final Optional<String> optionalName = validEntity.getName();
            if (optionalName.isPresent()) {
                if (replaceIf(byName(optionalName.get()).and(byUnique(true)), validEntity)) {
                    return this;
                }
            }
        }
        entityList.addLast(validEntity);
        return this;
    }
  
    @Override
    public List<Entity> asList() {
        return new ArrayList<>(entityList);
    }
    
    @Override
    public <T> Optional<T> findFirstValueWithType(Predicate<? super Entity> filter, Class<T> type) {
        final Predicate<? super Entity> validFilter = filterCheck(filter);
        final Class<T> validType = typeCheck(type);
        final AtomicReference<T> result = new AtomicReference<>();
        
        visitEach( entity -> {
            final Optional<Object> optionalValue = entity.getValue();
            if (validFilter.test(entity)) {
                if (optionalValue.isPresent()) {
                    final Object value = optionalValue.get();
                    if (validType.isInstance(value)) {
                        result.set(validType.cast(value));
                        return false;
                    }
                }
            }
            return true;
        });
        
        return Optional.ofNullable(result.get());
    }
    
    @Override
    public <T> List<T> findAllValuesWithType(Predicate<? super Entity> filter, Class<T> type) {
        final Predicate<? super Entity> validFilter = filterCheck(filter);
        final Class<T> validType = typeCheck(type);
        final LinkedList<T> matchedList = new LinkedList<>();
        
        visitEach( entity -> {
            final Optional<Object> optionalValue = entity.getValue();
            if (validFilter.test(entity)) {
                if (optionalValue.isPresent()) {
                    final Object value = optionalValue.get();
                    if (validType.isInstance(value)) {
                        matchedList.add(validType.cast(value));
                    }
                }
            }
            return true;
        });
        return matchedList.isEmpty() ? Collections.emptyList() : matchedList;
    }

    EntitiesImpl() {
    
    }
  
    private final LinkedList<Entity> entityList = new LinkedList<>();
}

package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.metalog.api.Entities;
import io.github.jonloucks.metalog.api.Entity;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.github.jonloucks.contracts.api.Checks.*;
import static io.github.jonloucks.metalog.impl.Internal.*;

final class EntitiesImpl implements Entities, Entities.Builder<EntitiesImpl> {

    @Override
    public void visitEach(Visitor<? super Entity> visitor) {
        final Visitor<? super Entity> validVisitor = nullCheck(visitor, "Visitor must be present.");
        for (Entity entity : list) {
            if (!validVisitor.visit(entity)) {
                return;
            }
        }
    }

    @Override
    public Optional<Entity> findFirstIf(Predicate<? super Entity> filter) {
        return list.stream().filter(filterCheck(filter)).findFirst();
    }

    @Override
    public List<Entity> findAllIf(Predicate<? super Entity> filter) {
        return list.stream().filter(filterCheck(filter)).collect(Collectors.toList());
    }
    
    @Override
    public boolean replaceIf(Predicate<? super Entity> filter, Entity entity) {
        final Predicate<? super Entity> validFilter = filterCheck(filter);
        final Entity validEntity = entityCheck(entity);
        boolean replaced = false;
        
        final ListIterator<Entity> iterator = list.listIterator();
        while (iterator.hasNext()) {
            final Entity nextEntity = iterator.next();
            if (validFilter.test(nextEntity)) {
                iterator.set(validEntity);
                replaced = true;
            }
        }
        return replaced;
    }
    
    @Override
    public boolean removeIf(Predicate<? super Entity> filter) {
        return list.removeIf(filterCheck(filter));
    }
    
    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }
    
    @Override
    public int size() {
        return list.size();
    }
    
    @Override
    public EntitiesImpl unique(boolean unique) {
        this.unique = unique;
        return this;
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
        if (unique) {
            final Optional<String> optionalName = validEntity.getName();
            if (optionalName.isPresent() && replaceIf(byName(optionalName.get()), validEntity)) {
                return this;
            }
        }
        list.addLast(validEntity);
        return this;
    }
    
    @Override
    public <T> Optional<T> findFirstByNameWithType(String name, Class<T> type) {
        return findFirstWithType(byName(name), typeCheck(type));
    }
    
    @Override
    public List<Entity> asList() {
        return new ArrayList<>(list);
    }
    
    @Override
    public <T> Optional<T> findFirstWithType(Predicate<? super Entity> filter, Class<T> type) {
        final Predicate<? super Entity> validFilter = filterCheck(filter);
        final Class<T> validType = typeCheck(type);
        final AtomicReference<T> result = new AtomicReference<>();
        
        visitEach( entity -> {
            final Optional<Object> optionalValue = entity.getValue();
            if (validFilter.test(entity)) {
                if (optionalValue.isPresent()) {
                    final Object value = optionalValue.get();
                    if (validType.isInstance(value) && validFilter.test(entity)) {
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
    public <T> List<T> findAllByNameWithType(String name, Class<T> type) {
        return findAllWithType(byName(name), typeCheck(type));
    }
    
    @Override
    public <T> List<T> findAllWithType(Predicate<? super Entity> filter, Class<T> type) {
        final Predicate<? super Entity> validFilter = filterCheck(filter);
        final Class<T> validType = typeCheck(type);
        final LinkedList<T> matchedList = new LinkedList<>();
        
        visitEach( entity -> {
            final Optional<Object> optionalValue = entity.getValue();
            if (validFilter.test(entity)) {
                if (optionalValue.isPresent()) {
                    final Object value = optionalValue.get();
                    if (validType.isInstance(value) && validFilter.test(entity)) {
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
  
    private final LinkedList<Entity> list = new LinkedList<>();
    private boolean unique;
}

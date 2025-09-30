package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.metalog.api.Entities;
import io.github.jonloucks.metalog.api.Entity;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.github.jonloucks.contracts.api.Checks.nullCheck;
import static io.github.jonloucks.metalog.impl.Internal.entityCheck;
import static java.util.Optional.ofNullable;

final class EntityImpl implements Entity, Entity.Builder<EntityImpl> {

    @Override
    public EntityImpl id(String id) {
        this.id = id;
        return this;
    }
    
    @Override
    public EntityImpl name(String name) {
        this.name = name;
        return this;
    }
    
    @Override
    public EntityImpl text(Supplier<CharSequence> textSupplier) {
        this.textSupplier = textSupplier;
        this.text = null;
        return this;
    }
    
    @Override
    public EntityImpl value(Object value) {
        this.value = value;
        this.text = null;
        return this;
    }
    
    @Override
    public Optional<Entities> getCorrelations() {
       return entityList.isEmpty() ? Optional.empty() : Optional.of(entityList);
    }
    
    @Override
    public EntityImpl correlation(Consumer<Builder<?>> action) {
        entityList.entity(action);
        return this;
    }
    
    @Override
    public EntityImpl correlation(Entity entity) {
        entityList.entity(entity);
        return this;
    }
    
    @Override
    public EntityImpl correlations(Consumer<Entities.Builder<?>> builder) {
        final Consumer<Entities.Builder<?>> validBuilder = nullCheck(builder, "builder was null");
        
        validBuilder.accept(entityList);
       
        return this;
    }
    
    @Override
    public EntityImpl copy(Entity fromEntity) {
        final Entity validFromEntity = entityCheck(fromEntity);
        
        fromEntity.getId().ifPresent(this::id);
        fromEntity.getName().ifPresent(this::name);
        fromEntity.getValue().ifPresent(this::value);
        
        validFromEntity.getCorrelations().ifPresent(c -> c.visitEach(e -> {
                correlation(e);
                return true;
            }
        ));
        return this;
    }
    
    @Override
    public Optional<String> getId() {
        return ofNullable(id);
    }
    
    @Override
    public Optional<String> getName() {
        return ofNullable(name);
    }
    
    @Override
    public Optional<Object> getValue() {
        return ofNullable(value);
    }

    @Override
    public CharSequence get() {
        if (null == text) {
            if (null == textSupplier) {
                if (null != value) {
                    text = value.toString();
                }
            } else {
                text = textSupplier.get();
            }
        }
        return text;
    }
    
    EntityImpl() {
    
    }

    private String id;
    private String name;
    private Supplier<CharSequence> textSupplier;
    private Object value;
    private CharSequence text;
    private final EntitiesImpl entityList = new EntitiesImpl();
}

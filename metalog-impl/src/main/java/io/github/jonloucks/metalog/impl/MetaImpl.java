package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.metalog.api.Entities;
import io.github.jonloucks.metalog.api.Entity;
import io.github.jonloucks.metalog.api.Meta;

import java.time.temporal.Temporal;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.github.jonloucks.contracts.api.Checks.typeCheck;
import static io.github.jonloucks.metalog.impl.Internal.*;

final class MetaImpl implements Meta.Builder<MetaImpl>, Entity.Builder<MetaImpl> {

    @Override
    public CharSequence get() {
        return thisEntity.get();
    }

    @Override
    public MetaImpl block(boolean block) {
        this.block = block;
        return this;
    }
    
    @Override
    public MetaImpl key(String key) {
        this.key = key;
        return this;
    }
    
    @Override
    public MetaImpl channel(String channel) {
        this.channel = channelCheck(channel);
        return this;
    }
    
    @Override
    public MetaImpl thread(Thread thread) {
        return setUniqueEntity(THREAD_ENTITY_NAME, thread);
    }
    
    @Override
    public MetaImpl thrown(Throwable thrown) {
        return setUniqueEntity(THROWN_ENTITY_NAME, thrown);
    }
    
    @Override
    public MetaImpl time(Temporal timestamp) {
        return setUniqueEntity(TIME_ENTITY_NAME, timestamp);
    }
    
    @Override
    public boolean hasBlock() {
        return block;
    }
    
    @Override
    public Optional<Temporal> getTime() {
        return getUniqueEntity(TIME_ENTITY_NAME, Temporal.class);
    }
    
    @Override
    public Optional<Throwable> getThrown() {
        return getUniqueEntity(THROWN_ENTITY_NAME, Throwable.class);
    }
    
    @Override
    public Optional<Thread> getThread() {
        return getUniqueEntity(THREAD_ENTITY_NAME, Thread.class);
    }
    
    @Override
    public Optional<String> getKey() {
        return Optional.ofNullable(key);
    }

    @Override
    public Optional<String> getId() {
        return thisEntity.getId();
    }
    
    @Override
    public Optional<String> getName() {
        return thisEntity.getName();
    }
    
    @Override
    public Optional<Object> getValue() {
        return thisEntity.getValue();
    }
    
    @Override
    public String getChannel() {
        return channel;
    }
    
    @Override
    public Optional<Entities> getCorrelations() {
        return thisEntity.getCorrelations();
    }
    
    @Override
    public boolean isUnique() {
        return thisEntity.isUnique();
    }
    
    @Override
    public MetaImpl name(String name) {
        thisEntity.name(name);
        return this;
    }
    
    @Override
    public MetaImpl unique(boolean unique) {
        thisEntity.unique(unique);
        return this;
    }
    
    @Override
    public MetaImpl text(Supplier<CharSequence> textSupplier) {
        thisEntity.text(textSupplier);
        return this;
    }
    
    @Override
    public MetaImpl value(Object value) {
        thisEntity.value(value);
        return this;
    }
    
    @Override
    public MetaImpl correlation(Consumer<Entity.Builder<?>> builderConsumer) {
        thisEntity.correlation(builderConsumer);
        return this;
    }
    
    @Override
    public MetaImpl correlation(Entity entity) {
        thisEntity.correlation(entity);
        return this;
    }
    
    @Override
    public MetaImpl correlations(Consumer<Entities.Builder<?>> builderConsumer) {
        thisEntity.correlations(builderConsumer);
        return this;
    }
    
    @Override
    public MetaImpl copy(Meta fromMeta) {
        final Meta validFromMeta = metaCheck(fromMeta);
        
        thisEntity.copy(validFromMeta);
        
        block(validFromMeta.hasBlock());
        channel(validFromMeta.getChannel());
        validFromMeta.getKey().ifPresent(this::key);
        
        return this;
    }
    
    @Override
    public MetaImpl copy(Entity entity) {
        thisEntity.copy(entity);
        return this;
    }
    
    @Override
    public MetaImpl id(String id) {
        thisEntity.id(id);
        return this;
    }
    
    MetaImpl() {
    }
    
    private <T> MetaImpl setUniqueEntity(String name, T value) {
        if (value == null) {
            return correlations( b -> b.removeIf(byName(name).and(byUnique())));
        } else {
            return correlation(b -> b.unique().name(name).value(value));
        }
    }
    
    private <T> Optional<T> getUniqueEntity(String name, Class<T> type) {
        final Optional<Entities> optional = getCorrelations();
        if (optional.isPresent()) {
            final Entities entities = optional.get();
            return entities.findFirstValueWithType(byName(name).and(byUnique()), typeCheck(type));
        }
        return Optional.empty();
    }
    
    private static final String THROWN_ENTITY_NAME = "thrown";
    private static final String TIME_ENTITY_NAME = "time";
    private static final String THREAD_ENTITY_NAME = "thread";
    
    private boolean block;
    private String key;
    private String channel = Meta.DEFAULT.getChannel();
    private final EntityImpl thisEntity = new EntityImpl();
}

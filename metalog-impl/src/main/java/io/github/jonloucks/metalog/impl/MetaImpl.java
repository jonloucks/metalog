package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.metalog.api.Entities;
import io.github.jonloucks.metalog.api.Entity;
import io.github.jonloucks.metalog.api.Meta;

import java.time.temporal.Temporal;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
        if (null == thread) {
            thisEntity.correlations( b -> b.removeIf(byName(THREAD_ENTITY_NAME)));
        } else {
            correlation(b -> b.name(THREAD_ENTITY_NAME).id(Long.toString(thread.getId())).value(thread));
        }
        return this;
    }
    
    @Override
    public MetaImpl thrown(Throwable thrown) {
        if (thrown == null) {
            thisEntity.correlations( b -> b.removeIf(byName(THROWN_ENTITY_NAME)));
        } else {
            return correlation(b -> b.name(THROWN_ENTITY_NAME).value(thrown));
        }
        return this;
    }
    
    @Override
    public MetaImpl time(Temporal timestamp) {
        if (timestamp == null) {
            thisEntity.correlations( b -> b.removeIf(byName(TIME_ENTITY_NAME)));
        } else {
            return correlation(b -> b.name(TIME_ENTITY_NAME).value(timestamp));
        }
        return this;
    }
    
    @Override
    public boolean isBlock() {
        return block;
    }
    
    @Override
    public Optional<Temporal> getTime() {
        return findFirstByNameAndType(thisEntity, TIME_ENTITY_NAME, Temporal.class);
    }
    
    @Override
    public Optional<Throwable> getThrown() {
        return findFirstByNameAndType(thisEntity, THROWN_ENTITY_NAME, Throwable.class);
    }
    
    @Override
    public Optional<Thread> getThread() {
        return findFirstByNameAndType(thisEntity, THREAD_ENTITY_NAME, Thread.class);
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
    public MetaImpl name(String name) {
        thisEntity.name(name);
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
        
        block(validFromMeta.isBlock());
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
    
    private static final String THROWN_ENTITY_NAME = "thrown";
    private static final String TIME_ENTITY_NAME = "time";
    private static final String THREAD_ENTITY_NAME = "thread";
    
    private boolean block;
    private String key;
    private String channel = "info";
    private final EntityImpl thisEntity = new EntityImpl();
}

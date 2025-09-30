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
    public MetaImpl blocking(boolean blocking) {
        this.blocking = blocking;
        return this;
    }
    
    @Override
    public MetaImpl sequenceKey(String sequenceKey) {
        this.sequenceKey = sequenceKey;
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
    public MetaImpl timestamp(Temporal timestamp) {
        if (timestamp == null) {
            thisEntity.correlations( b -> b.removeIf(byName(TIMESTAMP_ENTITY_NAME)));
        } else {
            return correlation(b -> b.name(TIMESTAMP_ENTITY_NAME).value(timestamp));
        }
        return this;
    }
    
    @Override
    public boolean isBlocking() {
        return blocking;
    }
    
    @Override
    public Optional<Temporal> getTimestamp() {
        return findFirstByNameAndType(thisEntity, TIMESTAMP_ENTITY_NAME, Temporal.class);
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
    public Optional<String> getSequenceKey() {
        return Optional.ofNullable(sequenceKey);
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
    public MetaImpl correlation(Consumer<Entity.Builder<?>> action) {
        thisEntity.correlation(action);
        return this;
    }
    
    @Override
    public MetaImpl correlation(Entity entity) {
        thisEntity.correlation(entity);
        return this;
    }
    
    @Override
    public MetaImpl correlations(Consumer<Entities.Builder<?>> builder) {
        thisEntity.correlations(builder);
        return this;
    }
    
    @Override
    public MetaImpl copy(Meta fromMeta) {
        final Meta validFromMeta = metaCheck(fromMeta);
        
        thisEntity.copy(validFromMeta);
        
        blocking(validFromMeta.isBlocking());
        channel(validFromMeta.getChannel());
        validFromMeta.getSequenceKey().ifPresent(this::sequenceKey);
        
        return this;
    }
    
    @Override
    public MetaImpl copy(Entity fromEntity) {
        thisEntity.copy(fromEntity);
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
    private static final String TIMESTAMP_ENTITY_NAME = "timestamp";
    private static final String THREAD_ENTITY_NAME = "thread";
    
    private boolean blocking;
    private String sequenceKey;
    private String channel = "info";
    private final EntityImpl thisEntity = new EntityImpl();
}

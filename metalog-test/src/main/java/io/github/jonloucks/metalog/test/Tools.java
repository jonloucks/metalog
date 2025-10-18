package io.github.jonloucks.metalog.test;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.Contracts;
import io.github.jonloucks.metalog.api.*;
import org.opentest4j.TestAbortedException;

import static io.github.jonloucks.contracts.test.Tools.*;
import static io.github.jonloucks.metalog.api.GlobalMetalog.findMetalogFactory;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.ArgumentMatchers.argThat;

public final class Tools {
    
    public static void clean() {
        io.github.jonloucks.contracts.test.Tools.clean();
        sanitize(()-> {
            final Metalog.Config config = new Metalog.Config() {};
            if (config.useServiceLoader()) {
                final ServiceLoader<? extends MetalogFactory> loader = ServiceLoader.load(config.serviceLoaderClass());
                loader.reload();
            }
        });
    }
    
    static String uniqueString() {
        return UUID.randomUUID().toString();
    }
    
    static Meta metaWithId(String id) {
        if (null == id) {
            return argThat(m -> !m.getId().isPresent());
        } else {
            return argThat(m -> m.getId().isPresent() && id.equals(m.getId().get()));
        }
    }
    
    static Meta metaWithIdThrown(String id, Throwable thrown) {
        return and(metaWithId(id), metaWithThrown(thrown));
    }
    
    static Meta metaWithThrown(Throwable thrown) {
        if (null == thrown) {
            return argThat(m -> !m.getThrown().isPresent());
        } else {
            return argThat(m -> m.getThrown().map(throwable -> throwable.equals(thrown)).orElse(false));
        }
    }
    
    static Entity createTestEntity(String name) {
        return createTestEntity(name, null);
    }
    
    static Entity createUniqueTestEntity(String name) {
        return createTestEntity(name, null, null, true);
    }
    
    static Entity createTestEntity(String name, Object value) {
        return createTestEntity(name, value, null, false);
    }
    
    static Entity createTestEntity(String name, Object value, String id, boolean unique) {
        return new Entity() {
            @Override
            public CharSequence get() {
                return "";
            }
            
            @Override
            public Optional<String> getId() {
                return Optional.ofNullable(id);
            }
            
            @Override
            public Optional<String> getName() {
                return Optional.ofNullable(name);
            }
            
            @Override
            public boolean isUnique() {
                return unique;
            }
            
            @Override
            public Optional<Object> getValue() {
                return Optional.ofNullable(value);
            }
        };
    }
    
    public static void assertMetaDefaults(Meta meta) {
        assertNotNull(meta.getChannel());
        assertEquals("info", meta.getChannel());
        assertEquals(Meta.DEFAULT.getName().isPresent(), meta.getName().isPresent());
        assertEquals(Meta.DEFAULT.isBlocking(), meta.isBlocking());
        assertEquals(Meta.DEFAULT.getValue().isPresent(), meta.getValue().isPresent());
        assertEquals(Meta.DEFAULT.getId().isPresent(), meta.getId().isPresent());
        assertEquals(Meta.DEFAULT.getThrown().isPresent(), meta.getThrown().isPresent());
        assertEquals(Meta.DEFAULT.getThread().isPresent(), meta.getThread().isPresent());
        assertEquals(Meta.DEFAULT.getTime().isPresent(), meta.getTime().isPresent());
        assertEquals(Meta.DEFAULT.getKey().isPresent(), meta.getKey().isPresent());
        assertEquals(Meta.DEFAULT.getCorrelations().isPresent(), meta.getCorrelations().isPresent());
        assertEquals(Meta.DEFAULT.get(), meta.get());
    }
    
    public static void assertOutcomeSuccess(Outcome outcome) {
        assertNotNull(outcome);
        assertTrue(outcome == Outcome.DISPATCHED || outcome == Outcome.CONSUMED,
            "Outcome should have been dispatched or consumed, but was " + outcome);
    }

    public static void assertOutcomeSkipped(Outcome outcome) {
        assertEquals(Outcome.SKIPPED, outcome, "Outcome should have been skipped");
    }
    
    public static void withMetalog(Consumer<Metalog.Config.Builder> builderConsumer, BiConsumer<Contracts,Metalog> block) {
        withContracts(contracts -> {
            final MetalogFactory factory = getMetalogFactory();
            final Metalog metalog = factory.create(b -> {
                b.contracts(contracts);
                builderConsumer.accept(b);
            });
            try (AutoClose closeMetalog = metalog.open()) {
                ignore(closeMetalog);
                block.accept(contracts, metalog);
            }
        });
    }

    public static void withMetalog(BiConsumer<Contracts,Metalog> block) {
        withMetalog( b->{}, block);
    }
    
    public static MetalogFactory getMetalogFactory() {
        return getMetalogFactory(Metalog.Config.DEFAULT);
    }
    
    public static MetalogFactory getMetalogFactory(Metalog.Config config) {
        return findMetalogFactory(config)
            .orElseThrow(() -> new TestAbortedException("Metalog Factory not found."));
    }
    
    public static void withMetalogInstalled(Consumer<Contracts> block) {
        withMetalog(b -> {}, (contracts, metalog) -> {
            block.accept(contracts);
        });
    }
    
    /**
     * Utility class instantiation protection
     */
    private Tools() {
        throw new AssertionError("Illegal constructor call.");
    }
}

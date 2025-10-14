package io.github.jonloucks.metalog.test;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.Contracts;
import io.github.jonloucks.contracts.api.GlobalContracts;
import io.github.jonloucks.metalog.api.*;

import static io.github.jonloucks.contracts.api.Checks.nullCheck;
import static io.github.jonloucks.contracts.test.Tools.sanitize;
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
    
    public static void assertOutcomeRejected(Outcome outcome) {
        assertEquals(Outcome.REJECTED, outcome, "Outcome should have been rejected");
    }
    
    public static void assertOutcomeConsumed(Outcome outcome) {
        assertEquals(Outcome.CONSUMED, outcome, "Outcome should have been consumed");
    }
    
    public static void assertOutcomeSkipped(Outcome outcome) {
        assertEquals(Outcome.SKIPPED, outcome, "Outcome should have been skipped");
    }
    
    public static void withContracts(Consumer<Contracts> block) {
        final Contracts.Config config = new Contracts.Config() {
            @Override
            public boolean useShutdownHooks() {
                return false;
            }
        };
        withContracts(config, block);
    }
    
    public static void withContracts(Contracts.Config config, Consumer<Contracts> block) {
        final Contracts.Config validConfig = nullCheck(config, "Config must be present.");
        final Consumer<Contracts> validBlock = nullCheck(block, "Block must be present.");
        final Contracts contracts = GlobalContracts.createContracts(validConfig);
        
        try (AutoClose closeContracts = contracts.open()) {
            final AutoClose ignored = closeContracts;
            validBlock.accept(contracts);
        }
    }
    
    public static void withMetalog(BiConsumer<Contracts,Metalog> block) {
        withContracts(contracts -> {
            final Metalog.Config config = new Metalog.Config() {
                @Override
                public Contracts contracts() {
                    return contracts;
                }
            };
            withMetalog(config, block);
        });

    }
    
    public static void withMetalog(Metalog.Config config, BiConsumer<Contracts,Metalog> block) {
        final Metalog.Config validConfig = nullCheck(config, "Config must be present.");
        final BiConsumer<Contracts, Metalog> validBlock = nullCheck(block, "Block must be present.");
        final Metalog metalog = GlobalMetalog.createMetalog(validConfig);
        
        try (AutoClose closeMetalog = metalog.open()) {
            final AutoClose ignored = closeMetalog;
            validBlock.accept(config.contracts(), metalog);
        }
    }

    /**
     * Utility class instantiation protection
     */
    private Tools() {
        throw new AssertionError("Illegal constructor call.");
    }
}

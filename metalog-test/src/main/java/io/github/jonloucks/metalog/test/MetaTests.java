package io.github.jonloucks.metalog.test;

import io.github.jonloucks.contracts.api.Contracts;
import io.github.jonloucks.metalog.api.Entity;
import io.github.jonloucks.metalog.api.Meta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.github.jonloucks.contracts.test.Tools.assertInstantiateThrows;
import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static io.github.jonloucks.metalog.test.MetaTests.MetaTestsTools.newMetaBuilder;
import static io.github.jonloucks.metalog.test.MetaTests.MetaTestsTools.runWithScenario;
import static io.github.jonloucks.metalog.test.Tools.createTestEntity;
import static io.github.jonloucks.metalog.test.Tools.withMetalog;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("CodeBlock2Expr")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public interface MetaTests {
    
    @Test
    default void meta_InitialValues() {
        runWithScenario(Tools::assertMetaDefaults);
    }
    
    @Test
    default void meta_copy_WithMeta1_Works() {
        runWithScenario(metaBuilder -> {
            final Throwable thrown = new RuntimeException("Oh no.");
            final Thread thread = Thread.currentThread();
            final Instant timestamp = Instant.now();
            final Meta.Builder<?> fromBuilder = newMetaBuilder();
            final Supplier<CharSequence> textSupplier = () -> "Alpha";
            
            fromBuilder
                .text(textSupplier)
                .id("id1")
                .name("name1")
                .value("value1")
                .channel("channel1")
                .key("sequenceKey1")
                .block()
                .thread(thread)
                .thrown(thrown)
                .unique(true)
                .time(timestamp);
            
            metaBuilder.copy(fromBuilder);
            
            assertEquals("value1", metaBuilder.get());
            assertTrue(metaBuilder.getName().isPresent());
            assertEquals("name1", metaBuilder.getName().get());
            assertTrue(metaBuilder.getId().isPresent());
            assertEquals("id1", metaBuilder.getId().get());
            assertTrue(metaBuilder.getValue().isPresent());
            assertEquals("value1", metaBuilder.getValue().get());
            assertEquals(fromBuilder.getChannel(), metaBuilder.getChannel());
            assertTrue(metaBuilder.getKey().isPresent());
            assertEquals("sequenceKey1", metaBuilder.getKey().get());
            assertTrue(metaBuilder.getCorrelations().isPresent());
            assertTrue(metaBuilder.getTime().isPresent());
            assertEquals(timestamp, metaBuilder.getTime().get());
            assertTrue(metaBuilder.getThrown().isPresent());
            assertEquals(thrown, metaBuilder.getThrown().get());
            assertEquals(fromBuilder.isBlocking(), metaBuilder.isBlocking());
            assertTrue(metaBuilder.getThread().isPresent());
            assertEquals(thread, metaBuilder.getThread().get());
            assertTrue(metaBuilder.isUnique());
        });
    }
    
    @Test
    default void meta_copy_WithMeta_Works() {
        runWithScenario(metaBuilder -> {
            final Throwable thrown = new RuntimeException("Oh no.");
            final Thread thread = Thread.currentThread();
            final Instant timestamp = Instant.now();
            metaBuilder.id("id1")
                .name("name1")
                .value("value1")
                .channel("channel1")
                .key("sequenceKey1")
                .block(true)
                .thread(thread)
                .thrown(thrown)
                .time(timestamp);
            
            final Meta.Builder<?> fromBuilder = newMetaBuilder();
            
            metaBuilder.copy(fromBuilder);
            
            assertTrue(metaBuilder.getName().isPresent());
            assertEquals("name1", metaBuilder.getName().get());
            assertTrue(metaBuilder.getId().isPresent());
            assertEquals("id1", metaBuilder.getId().get());
            assertTrue(metaBuilder.getValue().isPresent());
            assertEquals("value1", metaBuilder.getValue().get());
            assertEquals(fromBuilder.getChannel(), metaBuilder.getChannel());
            assertTrue(metaBuilder.getKey().isPresent());
            assertEquals("sequenceKey1", metaBuilder.getKey().get());
            assertTrue(metaBuilder.getCorrelations().isPresent());
            assertTrue(metaBuilder.getTime().isPresent());
            assertEquals(timestamp, metaBuilder.getTime().get());
            assertTrue(metaBuilder.getThrown().isPresent());
            assertEquals(thrown, metaBuilder.getThrown().get());
            assertEquals(fromBuilder.isBlocking(), metaBuilder.isBlocking());
            assertTrue(metaBuilder.getThread().isPresent());
            assertEquals(thread, metaBuilder.getThread().get());
        });
    }
    @Test
    default void meta_copy_WithEntity_Works() {
        runWithScenario(metaBuilder -> {
            final Throwable thrown = new RuntimeException("Oh no.");
            final Thread thread = Thread.currentThread();
            final Instant timestamp = Instant.now();
            final Meta.Builder<?> fromBuilder = newMetaBuilder();
            final Supplier<CharSequence> textSupplier = () -> "Alpha";
            
            fromBuilder
                .text(textSupplier)
                .id("id1")
                .name("name1")
                .value("value1")
                .channel("channel1")
                .key("sequenceKey1")
                .block(true)
                .thread(thread)
                .thrown(thrown)
                .time(timestamp);
            
            metaBuilder.copy((Entity.Builder<?>)fromBuilder);
            
            assertEquals("value1", metaBuilder.get());
            assertTrue(metaBuilder.getName().isPresent());
            assertEquals("name1", metaBuilder.getName().get());
            assertTrue(metaBuilder.getId().isPresent());
            assertEquals("id1", metaBuilder.getId().get());
            assertTrue(metaBuilder.getValue().isPresent());
            assertEquals("value1", metaBuilder.getValue().get());
            assertEquals("info", metaBuilder.getChannel());
            assertFalse(metaBuilder.getKey().isPresent());
//            assertEquals("sequenceKey1", meta.getSequenceKey().get());
            assertTrue(metaBuilder.getCorrelations().isPresent());
            assertTrue(metaBuilder.getTime().isPresent());
            assertEquals(timestamp, metaBuilder.getTime().get());
            assertTrue(metaBuilder.getThrown().isPresent());
            assertEquals(thrown, metaBuilder.getThrown().get());
            assertFalse(metaBuilder.isBlocking());
            assertTrue(metaBuilder.getThread().isPresent());
            assertEquals(thread, metaBuilder.getThread().get());
        });
    }
    
    @Test
    default void meta_time_NoArgs_Works() {
        runWithScenario( metaBuilder -> {
            final Meta.Builder<?> returnBuilder = metaBuilder.time();
            
            final Optional<Temporal> optionalTime = metaBuilder.getTime();
            
            assertSame(metaBuilder, returnBuilder );
            assertTrue(optionalTime.isPresent());
            assertNotNull(optionalTime.get());
        });
    }
    
    @Test
    default void meta_thread_NoArgs_Works() {
        runWithScenario( metaBuilder -> {
            final Meta.Builder<?> returnBuilder = metaBuilder.thread();
            
            final Optional<Thread> optionalThread = metaBuilder.getThread();
            
            assertSame(metaBuilder, returnBuilder );
            assertTrue(optionalThread.isPresent());
            assertEquals(Thread.currentThread(), optionalThread.get());
        });
    }
    
    
    @Test
    default void meta_time_WithTime_Works() {
        runWithScenario( metaBuilder -> {
            final Instant timestamp = Instant.now();
            
            final Meta.Builder<?> returnBuilder = metaBuilder.time(timestamp);
            
            final Optional<Temporal> optionalTime = metaBuilder.getTime();
            
            assertSame(metaBuilder, returnBuilder );
            assertTrue(optionalTime.isPresent());
            assertEquals(timestamp, optionalTime.get());
        });
    }
    
    @Test
    default void meta_thread_WithThreadWorks() {
        runWithScenario( metaBuilder -> {
            final Thread thread = Thread.currentThread();
            
            final Meta.Builder<?> returnBuilder = metaBuilder.thread(thread);
            
            final Optional<Thread> optionalThread = metaBuilder.getThread();
            
            assertSame(metaBuilder, returnBuilder );
            assertTrue(optionalThread.isPresent());
            assertEquals(thread, optionalThread.get());
        });
    }
    
    @Test
    default void meta_thrown_Works() {
        runWithScenario( metaBuilder -> {
            final Throwable thrown = new RuntimeException("Error");
            
            final Meta.Builder<?> returnBuilder = metaBuilder.thrown(thrown);
            
            final Optional<Throwable> optionalThrown = metaBuilder.getThrown();
            
            assertSame(metaBuilder, returnBuilder );
            assertTrue(optionalThrown.isPresent());
            assertEquals(thrown, optionalThrown.get());
        });
    }
    
    @Test
    default void meta_thrown_WithNullThrowable_Removes() {
        runWithScenario( metaBuilder -> {
            final Throwable thrown = new RuntimeException("Error");
            
            metaBuilder.thrown(thrown);
            final Meta.Builder<?> returnBuilder = metaBuilder.thrown(null);
            
            final Optional<Throwable> optionalThrown = metaBuilder.getThrown();
            
            assertSame(metaBuilder, returnBuilder );
            assertFalse(optionalThrown.isPresent());
        });
    }
    
    @Test
    default void meta_thread_WithNullThread_Removes() {
        runWithScenario( metaBuilder -> {
            final Thread thread = new Thread(()->{});
            metaBuilder.thread(thread);
            
            final Meta.Builder<?> returnBuilder = metaBuilder.thread(null);
            
            final Optional<Thread> optionalThread = metaBuilder.getThread();
            
            assertSame(metaBuilder, returnBuilder );
            assertFalse(optionalThread.isPresent());
        });
    }
    
    @Test
    default void meta_time_WithNullTime_Removes() {
        runWithScenario( metaBuilder -> {
            final Instant timestamp = Instant.now();
            
            metaBuilder.time(timestamp);
            final Meta.Builder<?> returnBuilder = metaBuilder.time(null);
            
            final Optional<Temporal> optionalTemporal = metaBuilder.getTime();
            
            assertSame(metaBuilder, returnBuilder );
            assertFalse(optionalTemporal.isPresent());
        });
    }
    
    @Test
    default void meta_correlation_WithNullEntity_Throws() {
        runWithScenario(builder -> {
            final IllegalArgumentException thrown = assertThrows( IllegalArgumentException.class, () -> {
                builder.correlation((Entity) null);
            });
            assertThrown(thrown);
        });
    }
    
    @Test
    default void meta_correlation_WithEntity_Works() {
        runWithScenario(builder -> {
            final Entity entity = createTestEntity("hello");
            
            final Entity.Builder<?> returnBuilder = builder.correlation(entity);
            
            assertNotNull(returnBuilder);
            assertTrue(builder.getCorrelations().isPresent());
            assertFalse(builder.getCorrelations().get().isEmpty());
            assertEquals(1, builder.getCorrelations().get().size());
            assertTrue(builder.getCorrelations().get().findFirstIf(p -> p == entity).isPresent());
        });
    }
    
    @Test
    default void meta_correlations_WithNullBuilder_Throws() {
        runWithScenario(builder -> {
            final IllegalArgumentException thrown = assertThrows( IllegalArgumentException.class, () -> {
                builder.correlations( null);
            });
            assertThrown(thrown);
        });
    }
    
    @Test
    default void meta_InternalCoverage() {
        assertInstantiateThrows(MetaTestsTools.class);
    }
    
    final class MetaTestsTools {
        private MetaTestsTools() {
            throw new AssertionError("Illegal constructor.");
        }
        
        @FunctionalInterface
        interface ScenarioConfig extends Consumer<Meta.Builder<?>> {
        }
        
        private static Contracts CONTRACTS;
        
        static Meta.Builder<?> newMetaBuilder() {
            return CONTRACTS.claim(Meta.Builder.FACTORY).get();
        }
        
        static void runWithScenario(ScenarioConfig scenarioConfig) {
            withMetalog(b -> {}, (contracts, metalog) -> {
                CONTRACTS = contracts;
                scenarioConfig.accept(newMetaBuilder());
            });
        }
    }
}

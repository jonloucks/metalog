package io.github.jonloucks.metalog.test;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.metalog.api.Entity;
import io.github.jonloucks.metalog.api.Meta;
import io.github.jonloucks.metalog.api.Metalogs;
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

import static io.github.jonloucks.contracts.api.GlobalContracts.claimContract;
import static io.github.jonloucks.metalog.api.GlobalMetalogs.createMetalogs;
import static io.github.jonloucks.metalog.test.MetaTests.MetaTestsTools.newMetaBuilder;
import static io.github.jonloucks.metalog.test.MetaTests.MetaTestsTools.runWithScenario;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public interface MetaTests {
    
    @Test
    default void meta_InitialValues() {
        runWithScenario( meta -> {
            assertFalse(meta.getName().isPresent());
            assertFalse(meta.getId().isPresent());
            assertFalse(meta.getValue().isPresent());
            assertEquals("info", meta.getChannel());
            assertFalse(meta.getSequenceKey().isPresent());
            assertFalse(meta.getCorrelations().isPresent());
            assertFalse(meta.isBlocking());
            assertFalse(meta.getThread().isPresent());
            assertFalse(meta.getThrown().isPresent());
            assertFalse(meta.getTimestamp().isPresent());
            assertNull(meta.get());
        });
    }
    
    @Test
    default void meta_copy_WithMeta1_Works() {
        runWithScenario(meta -> {
            final Throwable thrown = new RuntimeException("Oh no!");
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
                .sequenceKey("sequenceKey1")
                .blocking(true)
                .thread(thread)
                .thrown(thrown)
                .timestamp(timestamp);
            
            meta.copy(fromBuilder);
            
            assertEquals("value1", meta.get());
            assertTrue(meta.getName().isPresent());
            assertEquals("name1", meta.getName().get());
            assertTrue(meta.getId().isPresent());
            assertEquals("id1", meta.getId().get());
            assertTrue(meta.getValue().isPresent());
            assertEquals("value1", meta.getValue().get());
            assertEquals(fromBuilder.getChannel(), meta.getChannel());
            assertTrue(meta.getSequenceKey().isPresent());
            assertEquals("sequenceKey1", meta.getSequenceKey().get());
            assertTrue(meta.getCorrelations().isPresent());
            assertTrue(meta.getTimestamp().isPresent());
            assertEquals(timestamp, meta.getTimestamp().get());
            assertTrue(meta.getThrown().isPresent());
            assertEquals(thrown, meta.getThrown().get());
            assertEquals(fromBuilder.isBlocking(), meta.isBlocking());
            assertTrue(meta.getThread().isPresent());
            assertEquals(thread, meta.getThread().get());
        });
    }
    
    @Test
    default void meta_copy_WithMeta_Works() {
        runWithScenario(meta -> {
            final Throwable thrown = new RuntimeException("Oh no!");
            final Thread thread = Thread.currentThread();
            final Instant timestamp = Instant.now();
            meta.id("id1")
                .name("name1")
                .value("value1")
                .channel("channel1")
                .sequenceKey("sequenceKey1")
                .blocking(true)
                .thread(thread)
                .thrown(thrown)
                .timestamp(timestamp);
            
            final Meta.Builder<?> fromBuilder = newMetaBuilder();
            
            meta.copy(fromBuilder);
            
            assertTrue(meta.getName().isPresent());
            assertEquals("name1", meta.getName().get());
            assertTrue(meta.getId().isPresent());
            assertEquals("id1", meta.getId().get());
            assertTrue(meta.getValue().isPresent());
            assertEquals("value1", meta.getValue().get());
            assertEquals(fromBuilder.getChannel(), meta.getChannel());
            assertTrue(meta.getSequenceKey().isPresent());
            assertEquals("sequenceKey1", meta.getSequenceKey().get());
            assertTrue(meta.getCorrelations().isPresent());
            assertTrue(meta.getTimestamp().isPresent());
            assertEquals(timestamp, meta.getTimestamp().get());
            assertTrue(meta.getThrown().isPresent());
            assertEquals(thrown, meta.getThrown().get());
            assertEquals(fromBuilder.isBlocking(), meta.isBlocking());
            assertTrue(meta.getThread().isPresent());
            assertEquals(thread, meta.getThread().get());
        });
    }
    @Test
    default void meta_copy_WithEntity_Works() {
        runWithScenario(meta -> {
            final Throwable thrown = new RuntimeException("Oh no!");
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
                .sequenceKey("sequenceKey1")
                .blocking(true)
                .thread(thread)
                .thrown(thrown)
                .timestamp(timestamp);
            
            meta.copy((Entity.Builder<?>)fromBuilder);
            
            assertEquals("value1", meta.get());
            assertTrue(meta.getName().isPresent());
            assertEquals("name1", meta.getName().get());
            assertTrue(meta.getId().isPresent());
            assertEquals("id1", meta.getId().get());
            assertTrue(meta.getValue().isPresent());
            assertEquals("value1", meta.getValue().get());
            assertEquals("info", meta.getChannel());
            assertFalse(meta.getSequenceKey().isPresent());
//            assertEquals("sequenceKey1", meta.getSequenceKey().get());
            assertTrue(meta.getCorrelations().isPresent());
            assertTrue(meta.getTimestamp().isPresent());
            assertEquals(timestamp, meta.getTimestamp().get());
            assertTrue(meta.getThrown().isPresent());
            assertEquals(thrown, meta.getThrown().get());
            assertFalse(meta.isBlocking());
            assertTrue(meta.getThread().isPresent());
            assertEquals(thread, meta.getThread().get());
        });
    }
    
    
    @Test
    default void meta_Timestamp_Works() {
        runWithScenario( meta -> {
            final Instant timestamp = Instant.now();
            
            meta.timestamp(timestamp);
            
            final Optional<Temporal> optionalTimestamp = meta.getTimestamp();
            
            assertTrue(optionalTimestamp.isPresent());
            assertEquals(timestamp, optionalTimestamp.get());
        });
    }
    
    @Test
    default void meta_Thread_Works() {
        runWithScenario( meta -> {
            final Thread thread = Thread.currentThread();
            
            meta.thread(thread);
            
            final Optional<Thread> optionalThread = meta.getThread();
            
            assertTrue(optionalThread.isPresent());
            assertEquals(thread, optionalThread.get());
        });
    }
    
    @Test
    default void meta_Throwable_Works() {
        runWithScenario( meta -> {
            final Throwable thrown = new RuntimeException("Error");
            
            meta.thrown(thrown);
            
            final Optional<Throwable> optionalThrown = meta.getThrown();
            
            assertTrue(optionalThrown.isPresent());
            assertEquals(thrown, optionalThrown.get());
        });
    }
    
    final class MetaTestsTools {
        private MetaTestsTools() {
        }
        
        @FunctionalInterface
        interface ScenarioConfig extends Consumer<Meta.Builder<?>> {
            default Metalogs.Config getMetalogsConfig() {
                return Metalogs.Config.DEFAULT;
            }
        }
        
        static Meta.Builder<?> newMetaBuilder() {
            return claimContract(Meta.Builder.FACTORY_CONTRACT).get();
        }
        
        static void runWithScenario(ScenarioConfig scenarioConfig) {
            final Metalogs metalogs = createMetalogs(scenarioConfig.getMetalogsConfig());
            try (AutoClose closeLogs = metalogs.open()) {
                AutoClose ignoreWarning = closeLogs;
                scenarioConfig.accept(newMetaBuilder());
            }
        }
    }
}

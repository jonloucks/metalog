package io.github.jonloucks.metalog.test;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.metalog.api.Entity;
import io.github.jonloucks.metalog.api.Metalog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.function.Consumer;

import static io.github.jonloucks.contracts.api.GlobalContracts.claimContract;
import static io.github.jonloucks.contracts.test.Tools.assertInstantiateThrows;
import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static io.github.jonloucks.metalog.api.GlobalMetalog.createMetalog;
import static io.github.jonloucks.metalog.test.EntityTests.EntityTestsTools.newEntityBuilder;
import static io.github.jonloucks.metalog.test.EntityTests.EntityTestsTools.runWithScenario;
import static io.github.jonloucks.metalog.test.Tools.createTestEntity;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("CodeBlock2Expr")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public interface EntityTests {
    
    @Test
    default void entity_Builder_Defaults() {
        runWithScenario( builder -> {
            assertFalse(builder.getId().isPresent());
            assertFalse(builder.getName().isPresent());
            assertFalse(builder.getValue().isPresent());
            assertFalse(builder.getCorrelations().isPresent());
            assertFalse(builder.isUnique());
            assertNotNull(builder.get());
        });
    }
    
    @Test
    default void entity_Defaults() {
        final Entity entity = () -> null;
        assertFalse(entity.getId().isPresent());
        assertFalse(entity.getName().isPresent());
        assertFalse(entity.getValue().isPresent());
        assertFalse(entity.getCorrelations().isPresent());
        assertFalse(entity.isUnique());
    }
    
    @Test
    default void entity_unique_works() {
        runWithScenario( builder -> {
            builder.unique(true);
            assertTrue(builder.isUnique());
        });
    }
    
    @Test
    default void entity_text_ValueChanges_TextChanges() {
        runWithScenario( builder -> {
            builder.value(2);
            builder.get();
            builder.value(3);
            
            assertEquals("3", builder.get());
        });
    }
    
    @Test
    default void entity_text_Works() {
        runWithScenario( builder -> {
            builder.text(() -> "abc");
     
            assertEquals("abc", builder.get());
        });
    }
    
    @Test
    default void entity_text_Twice_Works() {
        runWithScenario( builder -> {
            builder.text(() -> "abc");
            
            assertEquals("abc", builder.get());
            assertEquals("abc", builder.get());
        });
    }
    
    @Test
    default void entity_text_WithNull_Removes() {
        runWithScenario( builder -> {
            builder.text(() -> "abc");
            builder.text(null);
            builder.value(3);
            
            assertEquals("3", builder.get());
        });
    }
    
    @Test
    default void entity_id_WithNull_RemovesValue() {
        runWithScenario( builder -> {
            builder.id("a");
            builder.id(null);
        
            assertFalse(builder.getId().isPresent());
        });
    }
    
    @Test
    default void entity_correlation_WithNullEntity_Throws() {
        runWithScenario( builder -> {
            final IllegalArgumentException thrown = assertThrows( IllegalArgumentException.class, () -> {
                builder.correlation((Entity) null);
            });
            assertThrown(thrown);
        });
    }
    
    @Test
    default void entity_copy_WithNullEntity_Throws() {
        runWithScenario( builder -> {
            final IllegalArgumentException thrown = assertThrows( IllegalArgumentException.class, () -> {
                builder.copy( null);
            });
            assertThrown(thrown);
        });
    }
    
    @Test
    default void entity_copy_OverwritesWhenPresent() {
        final Entity fromEntity = createTestEntity("from name", "from value", "from id", true);
        runWithScenario( builder -> {
            builder.id("original id").name("original name").value("original value");
            final Entity.Builder<?> result = builder.copy(fromEntity);
            
            assertNotNull(result);
            assertTrue(builder.getId().isPresent());
            assertEquals("from id", builder.getId().get());
            assertTrue(builder.getName().isPresent());
            assertEquals("from name", builder.getName().get());
            assertTrue(builder.getValue().isPresent());
            assertEquals("from value", builder.getValue().get());
            assertEquals(fromEntity.isUnique(), builder.isUnique());
        });
    }
    
    @Test
    default void entity_copy_DiscardsWhenNotPresent() {
        final Entity fromEntity = createTestEntity(null, null, null, false);
        runWithScenario( builder -> {
            builder.id("original id").name("original name").value("original value");
            final Entity.Builder<?> result = builder.copy(fromEntity);
            
            assertNotNull(result);
            assertTrue(builder.getId().isPresent());
            assertEquals("original id", builder.getId().get());
            assertTrue(builder.getName().isPresent());
            assertEquals("original name", builder.getName().get());
            assertTrue(builder.getValue().isPresent());
            assertEquals("original value", builder.getValue().get());
        });
    }
    
    @Test
    default void entity_copy_SetsNotPresent() {
        final Entity fromEntity = createTestEntity("from name", "from value", "from id", false);
        runWithScenario( builder -> {
            final Entity.Builder<?> result = builder.copy(fromEntity);
            
            assertNotNull(result);
            assertTrue(builder.getId().isPresent());
            assertEquals("from id", builder.getId().get());
            assertTrue(builder.getName().isPresent());
            assertEquals("from name", builder.getName().get());
            assertTrue(builder.getValue().isPresent());
            assertEquals("from value", builder.getValue().get());
        });
    }
    
    @Test
    default void entity_copy_WithCorrelations() {
        runWithScenario( builder -> {
            final Entity.Builder<?> fromEntity = newEntityBuilder();
            fromEntity.correlation(b -> b.name("c1").value("v1"));
            fromEntity.correlation(b -> b.name("c2").value("v2"));
            final Entity.Builder<?> result = builder.copy(fromEntity);
            
            assertNotNull(result);
            assertTrue(builder.getCorrelations().isPresent());
            assertFalse(builder.getCorrelations().get().isEmpty());
            assertEquals(2, builder.getCorrelations().get().size());
        });
    }
    
    @Test
    default void entity_correlation_WithNullAction_Throws() {
        runWithScenario( builder -> {
            final IllegalArgumentException thrown = assertThrows( IllegalArgumentException.class, () -> {
                builder.correlation((Consumer<Entity.Builder<?>>) null);
            });
            assertThrown(thrown);
        });
    }
    
    @Test
    default void entity_correlation_WithEntity() {
        runWithScenario( builder -> {
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
    default void entity_correlations_WithNullAction_Throws() {
        runWithScenario( builder -> {
            final IllegalArgumentException thrown = assertThrows( IllegalArgumentException.class, () -> {
                builder.correlations(null);
            });
            assertThrown(thrown);
        });
    }
    
    @Test
    default void entity_correlations_Action() {
        runWithScenario( builder -> {
            final Entity.Builder<?> resultBuilder = builder.correlations(list -> {
                list.entity(e -> {
                    e.name("hello");
                });
            });
            
            assertTrue(builder.getCorrelations().isPresent());
            assertEquals(1, builder.getCorrelations().get().size());
            assertNotNull(resultBuilder);
        });
    }
    
    @Test
    default void entity_InternalCoverage() {
        assertInstantiateThrows(EntityTestsTools.class);
    }
    
    final class EntityTestsTools {
        private EntityTestsTools() {
            throw new AssertionError("Illegal constructor");
        }
        
        @FunctionalInterface
        interface ScenarioConfig extends Consumer<Entity.Builder<?>> {
            default Metalog.Config getMetalogConfig() {
                return Metalog.Config.DEFAULT;
            }
        }
        
        static Entity.Builder<?> newEntityBuilder() {
            return claimContract(Entity.Builder.FACTORY_CONTRACT).get();
        }
        
        static void runWithScenario(ScenarioConfig scenarioConfig) {
            final Metalog metalog = createMetalog(scenarioConfig.getMetalogConfig());
            try (AutoClose closeLogs = metalog.open()) {
                AutoClose ignoreWarning = closeLogs;
                scenarioConfig.accept(newEntityBuilder());
            }
        }
    }
}

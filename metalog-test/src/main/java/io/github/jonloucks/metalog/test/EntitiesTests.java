package io.github.jonloucks.metalog.test;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.metalog.api.Entities;
import io.github.jonloucks.metalog.api.Entity;
import io.github.jonloucks.metalog.api.Metalogs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.github.jonloucks.contracts.api.GlobalContracts.claimContract;
import static io.github.jonloucks.contracts.test.Tools.assertObject;
import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static io.github.jonloucks.metalog.api.GlobalMetalogs.createMetalogs;
import static io.github.jonloucks.metalog.test.EntitiesTests.EntitiesTestsTools.runWithScenario;
import static io.github.jonloucks.metalog.test.Tools.createTestEntity;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"Convert2MethodRef", "CodeBlock2Expr"})
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public interface EntitiesTests {
    
    @Test
    default void entities_IsObject() {
        runWithScenario( entities -> {
            assertObject(entities);
        });
    }
    
    @Test
    default void entities_entity_WithNullEntity_Throws() {
        runWithScenario( entities -> {
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                entities.entity((Entity) null);
            });
            
            assertThrown(thrown);
        });
    }
    
    @Test
    default void entities_entity_WithMany_Works() {
        runWithScenario( entities -> {
            final List<Entity> expected = new ArrayList<>();
            final int entityCount = 1234;
            
            entities.unique(false);
            
            for (int i = 0; i < entityCount; i++) {
                final Entity entity = createTestEntity("same name");
                
                entities.entity(entity);
                expected.add(entity);
            }

            assertFalse(entities.isEmpty(), "Entities should not be empty");
            assertEquals(entityCount, entities.size(), "Entities size ");
            final List<Entity> found = entities.asList();
            assertNotNull(found);
            assertEquals(entityCount, found.size(), "Found size");
            assertEquals(expected, found, "Entities should be equal to");
        });
    }
    
    @Test
    default void entities_entity_WithManyMixed_Works() {
        runWithScenario( entities -> {
            final int entityCount = 1234;
            
            entities.unique(true);
            
            final int uniqueNames = 5;
            
            for (int i = 0; i < entityCount; i++) {
                entities.entity(createTestEntity("entity" + (i%uniqueNames)));
            }
            
            assertFalse(entities.isEmpty(), "Entities should be empty");
            assertEquals(uniqueNames, entities.size(), "Entities size ");
            final List<Entity> found = entities.findAllIf(p -> true);
            assertNotNull(found);
            assertEquals(uniqueNames, found.size(), "Found size");
        });
    }
    
    @Test
    default void entities_entity_WithManyMixed_Worksx() {
        runWithScenario( entities -> {
            final List<Entity> expected = new ArrayList<>();
            final int entityCount = 100;
            
            entities.unique(false);
            
            for (int i = 0; i < entityCount; i++) {
                final Object value;
                switch (i%5) {
                    case 0:
                        value = null;
                        break;
                    case 1:
                        value = Instant.now();
                        break;
                    default:
                        value = i;
                        break;
                }
                final Entity entity = createTestEntity("same name", value);
                
                entities.entity(entity);
                expected.add(entity);
            }
            
            final List<Integer> integers = entities.findAllWithType(e->true, Integer.class);
            final List<Instant>  instants = entities.findAllWithType(e->true, Instant.class);
            final List<Boolean> booleans = entities.findAllWithType(e->true, Boolean.class);
            final List<Temporal> temporals = entities.findAllWithType(e->true, Temporal.class);
            final List<Integer> failedIntegers = entities.findAllWithType(e->false, Integer.class);
            
            assertAll(
                () -> assertEquals(60, integers.size(), "Integers size "),
                () -> assertEquals(20, instants.size(), "Instants size "),
                () -> assertEquals(0, booleans.size(), "Booleans size "),
                () -> assertEquals(20, temporals.size(), "Temporals size "),
                () -> assertEquals(0, failedIntegers.size(), "Failed Integers size")
            );
        });
    }
    
    @Test
    default void entities_entity_WithNullAction_Throws() {
        runWithScenario( entities -> {
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                entities.entity((Consumer<Entity.Builder<?>>) null);
            });
            
            assertThrown(thrown);
        });
    }
    
    @Test
    default void entities_replaceIf_WithNullFilter_Throws() {
        final Entity entity = () -> "";
        runWithScenario( entities -> {
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                entities.replaceIf(null, entity);
            });
            
            assertThrown(thrown);
        });
    }
    
    @Test
    default void entities_replaceIf_WithNullEntity_Throws() {
        final Predicate<? super Entity> filter = e -> true;
        runWithScenario( entities -> {
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                entities.replaceIf(filter, null);
            });
            
            assertThrown(thrown);
        });
    }

    @Test
    default void entities_visitEach_WithNullVisitor_Throws() {
        runWithScenario( entities -> {
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                entities.visitEach(null);
            });
            
            assertThrown(thrown);
        });
    }
    
    @Test
    default void entities_removeIf_WithNullFilter_Throws() {
        runWithScenario( entities -> {
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                entities.removeIf(null);
            });
            
            assertThrown(thrown);
        });
    }
    
    @Test
    default void entities_findAllIf_WithNullFilter_Throws() {
        runWithScenario( entities -> {
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                entities.findAllIf(null);
            });
            
            assertThrown(thrown);
        });
    }
    
    @Test
    default void entities_findAllWithType_WithNullFilter_Throws() {
        runWithScenario( entities -> {
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                entities.findAllWithType(null, String.class);
            });
            
            assertThrown(thrown);
        });
    }
    
    @Test
    default void entities_findAllWithType_WithNullType_Throws() {
        final Predicate<? super Entity> filter = e -> true;
        
        runWithScenario( entities -> {
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                entities.findAllWithType(filter, null);
            });
            
            assertThrown(thrown);
        });
    }
    
    //    public <T> List<T> findAllWithType(Predicate<? super Entity> filter, Class<T> type) {
    
    @Test
    default void entities_findAllByNameWithType_WithNullName_Throws() {
        final Class<String> type = String.class;
        runWithScenario( entities -> {
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                entities.findAllByNameWithType(null, type);
            });
            
            assertThrown(thrown);
        });
    }
    
    @Test
    default void entities_findFirstIf_WithNullFilter_Throws() {
        final Class<String> type = String.class;
        runWithScenario( entities -> {
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                entities.findFirstIf(null);
            });
            
            assertThrown(thrown);
        });
    }
    
    final class EntitiesTestsTools {
        private EntitiesTestsTools() {
        }
        
        @FunctionalInterface
        interface ScenarioConfig extends Consumer<Entities.Builder<?>> {
            default Metalogs.Config getMetalogsConfig() {
                return Metalogs.Config.DEFAULT;
            }
        }
        
        static void runWithScenario(ScenarioConfig scenarioConfig) {
            final Metalogs metalogs = createMetalogs(scenarioConfig.getMetalogsConfig());
            try (AutoClose closeLogs = metalogs.open()) {
                AutoClose ignoreWarning = closeLogs;
                scenarioConfig.accept(claimContract(Entities.Builder.FACTORY_CONTRACT).get());
            }
        }
    }
}

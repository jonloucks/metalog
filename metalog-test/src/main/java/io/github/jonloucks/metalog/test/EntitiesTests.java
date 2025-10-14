package io.github.jonloucks.metalog.test;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.metalog.api.Entities;
import io.github.jonloucks.metalog.api.Entity;
import io.github.jonloucks.metalog.api.Metalog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.github.jonloucks.contracts.api.GlobalContracts.claimContract;
import static io.github.jonloucks.contracts.test.Tools.*;
import static io.github.jonloucks.metalog.api.GlobalMetalog.createMetalog;
import static io.github.jonloucks.metalog.test.EntitiesTests.EntitiesTestsTools.runWithScenario;
import static io.github.jonloucks.metalog.test.Tools.createTestEntity;
import static io.github.jonloucks.metalog.test.Tools.createUniqueTestEntity;
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
    default void entities_entity_AsBuilderWithUniqueAndWithoutName_Works() {
        runWithScenario( entitiesBuilder -> {
            final Entities.Builder<?> returnBuilder = entitiesBuilder.entity(b -> b.unique());
     
            assertNotNull(returnBuilder);
            assertFalse(entitiesBuilder.isEmpty(), "Entities should not be empty");
            assertEquals(1, entitiesBuilder.size(), "Entities size ");
            final Optional<Entity> optionalEntity = entitiesBuilder.findFirstIf(e -> true);
            assertTrue(optionalEntity.isPresent(), "Entity should be present");
            assertFalse(optionalEntity.get().getName().isPresent(), "Entity name is present");
        });
    }
    
    @Test
    default void entities_entity_WithMany_Works() {
        runWithScenario( entitiesBuilder -> {
            final List<Entity> expected = new ArrayList<>();
            final int entityCount = 1234;
            
            for (int i = 0; i < entityCount; i++) {
                final Entity entity = createTestEntity("same name");
                
                entitiesBuilder.entity(entity);
                expected.add(entity);
            }

            assertFalse(entitiesBuilder.isEmpty(), "Entities should not be empty");
            assertEquals(entityCount, entitiesBuilder.size(), "Entities size ");
            final List<Entity> found = entitiesBuilder.asList();
            assertNotNull(found);
            assertEquals(entityCount, found.size(), "Found size");
            assertEquals(expected, found, "Entities should be equal to");
        });
    }
    
    @Test
    default void entities_entity_WithManyMixed_Works() {
        runWithScenario( entities -> {
            final int entityCount = 1234;
            final int uniqueNames = 5;
            
            for (int i = 0; i < entityCount; i++) {
                entities.entity(createUniqueTestEntity("entity" + (i%uniqueNames)));
            }
            
            assertFalse(entities.isEmpty(), "Entities should not be empty");
            assertEquals(uniqueNames, entities.size(), "Entities size");
            final List<Entity> found = entities.findAllIf(p -> true);
            assertNotNull(found);
            assertEquals(uniqueNames, found.size(), "Found size");
        });
    }
    
    @Test
    default void entities_entity_WithManyMixedValues_Works() {
        runWithScenario( entities -> {
            final int entityCount = 100;
           
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
            }
            
            final List<Integer> integers = entities.findAllValuesWithType(e->true, Integer.class);
            final Optional<Integer> optionalInteger = entities.findFirstValueWithType(e->true, Integer.class);
            final List<Instant>  instants = entities.findAllValuesWithType(e->true, Instant.class);
            final Optional<Instant> optionalInstant = entities.findFirstValueWithType(e->true, Instant.class);
            final List<Boolean> booleans = entities.findAllValuesWithType(e->true, Boolean.class);
            final Optional<Boolean> optionalBoolean = entities.findFirstValueWithType(e->true, Boolean.class);
            final List<Temporal> temporals = entities.findAllValuesWithType(e->true, Temporal.class);
            final Optional<Temporal> optionalTemporal = entities.findFirstValueWithType(e->true, Temporal.class);
            final List<Integer> failedIntegers = entities.findAllValuesWithType(e->false, Integer.class);
            
            assertAll(
                () -> assertTrue(optionalInteger.isPresent(), "One of the integer values should be present"),
                () -> assertTrue(optionalInstant.isPresent(), "One of the instant values should be present"),
                () -> assertFalse(optionalBoolean.isPresent(), "Zero boolean values should be present"),
                () -> assertTrue(optionalTemporal.isPresent(), "One of the temporal values should be present")
            );
            
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
    default void entities_findAllWithType_Values_WithNullFilter_Throws() {
        runWithScenario( entities -> {
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                entities.findAllValuesWithType(null, String.class);
            });
            
            assertThrown(thrown);
        });
    }
    
    @Test
    default void entities_findAllWithType_Values_WithNullType_Throws() {
        final Predicate<? super Entity> filter = e -> true;
        
        runWithScenario( entities -> {
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                entities.findAllValuesWithType(filter, null);
            });
            
            assertThrown(thrown);
        });
    }
    
    
    @Test
    default void entities_findFirstIf_WithNullFilter_Throws() {
        runWithScenario( entities -> {
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                entities.findFirstIf(null);
            });
            
            assertThrown(thrown);
        });
    }
    
    @Test
    default void entities_InternalCoverage() {
        assertInstantiateThrows(EntitiesTestsTools.class);
    }
    
    final class EntitiesTestsTools {
        private EntitiesTestsTools() {
            throw new AssertionError("Illegal constructor");
        }
        
        @FunctionalInterface
        interface ScenarioConfig extends Consumer<Entities.Builder<?>> {
            default Metalog.Config getMetalogConfig() {
                return Metalog.Config.DEFAULT;
            }
        }
        
        static void runWithScenario(ScenarioConfig scenarioConfig) {
            final Metalog metalog = createMetalog(scenarioConfig.getMetalogConfig());
            try (AutoClose closeLogs = metalog.open()) {
                AutoClose ignoreWarning = closeLogs;
                scenarioConfig.accept(claimContract(Entities.Builder.FACTORY_CONTRACT).get());
            }
        }
    }
}

package io.github.jonloucks.metalog.test;

import io.github.jonloucks.contracts.api.*;
import io.github.jonloucks.metalog.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.util.stream.Stream;

import static io.github.jonloucks.contracts.test.Tools.*;
import static io.github.jonloucks.metalog.test.Tools.metaWithId;
import static io.github.jonloucks.metalog.test.Tools.uniqueString;
import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.*;

@SuppressWarnings("CodeBlock2Expr")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public interface GlobalMetalogTests {
    
    @Test
    default void globalMetalog_Instantiate_Throws() {
        assertInstantiateThrows(GlobalMetalog.class);
    }
    
    @Test
    default void globalMetalog_getInstance_Works() {
        assertObject(GlobalMetalog.getInstance());
    }
 
    @Test
    default void globalMetalog_LogWithBuild_RoundTrip(@Mock Subscriber subscriber, @Mock Log log) {
        final String text = uniqueString();
        when(log.get()).thenReturn(text);
        when(subscriber.test(any())).thenReturn(true);
        final String id = uniqueString();
        
        try (AutoClose closeSubscriber = GlobalMetalog.subscribe(subscriber)) {
            final AutoClose ignored = closeSubscriber;
            GlobalMetalog.publish(log, b -> b.id(id).block());
            verify(subscriber, times(1)).receive(any(), metaWithId(id));
        }
        GlobalMetalog.publish(log, b -> b.id(id).block());
        verify(subscriber, times(1)).receive(any(), metaWithId(id));
    }
    
    @Test
    default void globalMetalog_Log_DoesNotThrow() {
        assertDoesNotThrow(() -> GlobalMetalog.publish((() -> "hi")));
    }
    
    @Test
    default void globalMetalog_LogAndMeta_DoesNotThrow() {
        assertDoesNotThrow(() -> GlobalMetalog.publish((() -> "hi"), Meta.DEFAULT));
    }
    
    @Test
    default void globalMetalog_LogAndBuilder_DoesNotThrow() {
        assertDoesNotThrow(() -> GlobalMetalog.publish((() -> "hi"), b -> b.name("x")));
    }
    
    @Test
    default void globalMetalog_DefaultConfig() {
        final Metalog.Config config = new Metalog.Config() {};
        
        assertAll(
            () -> assertTrue(config.useReflection(), "config.useReflection() default."),
            () -> assertTrue(config.useServiceLoader(), "config.useServiceLoader() default."),
            () -> assertNotNull(config.reflectionClassName(), "config.reflectionClassName() was null."),
            () -> assertEquals(MetalogFactory.class, config.serviceLoaderClass(), "config.serviceLoaderClass() default."),
            () -> assertEquals(GlobalContracts.getInstance(), config.contracts(), "config.contracts()  default."),
            () -> assertEquals(1_000, config.keyedQueueLimit(), "config.keyedQueueLimit() default."),
            () -> assertEquals(10, config.unkeyedThreadCount(), "config.unkeyedThreadCount() default."),
            () -> assertFalse(config.unkeyedFairness(), "config.unkeyedFairness() default."),
            () -> assertEquals(Duration.ofSeconds(60), config.shutdownTimeout(), "config.shutdownTimeout() default."),
            () -> assertTrue(config.activeConsole(), "config.activeConsole() default.")
        );
    }
    
    @ParameterizedTest
    @MethodSource("io.github.jonloucks.metalog.test.GlobalMetalogTests$GlobalMetalogTestsTools#validConfigs")
    default void globalMetalog_HappyPath(Metalog.Config metalogConfig) {
        final Metalog metalog = GlobalMetalog.createMetalog(metalogConfig);
        
        assumeTrue(ofNullable(metalog).isPresent(), "createContracts failed");
        
        try (AutoClose closeMetalog = metalog.open()) {
            final AutoClose ignored = closeMetalog;
            
            final Contracts contracts = metalogConfig.contracts();
            
            assertTrue(contracts.isBound(Metalog.CONTRACT));
            assertTrue(contracts.isBound(MetalogFactory.CONTRACT));
            assertTrue(contracts.isBound(Entities.Builder.FACTORY_CONTRACT));
            assertTrue(contracts.isBound(Entity.Builder.FACTORY_CONTRACT));
            assertTrue(contracts.isBound(Meta.Builder.FACTORY_CONTRACT));
            
            if (metalogConfig.activeConsole()) {
                assertTrue(contracts.isBound(Console.CONTRACT));
            }
        }
    }
    
    @ParameterizedTest
    @MethodSource("io.github.jonloucks.metalog.test.GlobalMetalogTests$GlobalMetalogTestsTools#invalidConfigs")
    default void globalMetalog_SadPath(Metalog.Config metalogConfig) {
        final ContractException thrown = assertThrows(ContractException.class, () -> {
            GlobalMetalog.createMetalog(metalogConfig);
        });
        
        assertThrown(thrown);
    }
    
    @Test
    default void globalMetalog_InternalCoverage() {
        assertInstantiateThrows(GlobalMetalogTests.GlobalMetalogTestsTools.class);
    }
    
    final class GlobalMetalogTestsTools {
        private GlobalMetalogTestsTools() {
            throw new AssertionError("Illegal constructor");
        }
        @SuppressWarnings("RedundantMethodOverride")
        static Stream<Arguments> validConfigs() {
            return Stream.of(
                Arguments.of(new Metalog.Config() {}),
                Arguments.of(new Metalog.Config() {
                    @Override
                    public boolean useServiceLoader() {
                        return false;
                    }
                    @Override
                    public boolean useReflection() {
                        return true;
                    }
                }),
                Arguments.of(new Metalog.Config() {
                    @Override
                    public boolean useServiceLoader() {
                        return true;
                    }
                    @Override
                    public boolean useReflection() {
                        return false;
                    }
                })
            );
        }
        
        @SuppressWarnings("RedundantMethodOverride")
        static Stream<Arguments> invalidConfigs() {
            return Stream.of(
                Arguments.of(new Metalog.Config() {
                    @Override
                    public boolean useServiceLoader() {
                        return false;
                    }
                    @Override
                    public boolean useReflection() {
                        return false;
                    }
                }),
                Arguments.of(new Metalog.Config() {
                    @Override
                    public boolean useServiceLoader() {
                        return true;
                    }
                    @Override
                    public boolean useReflection() {
                        return false;
                    }
                    @Override
                    public Class<? extends MetalogFactory> serviceLoaderClass() {
                        return BadMetalogFactory.class;
                    }
                }),
                Arguments.of(new Metalog.Config() {
                    @Override
                    public boolean useServiceLoader() {
                        return false;
                    }
                    @Override
                    public boolean useReflection() {
                        return true;
                    }
                    @Override
                    public String reflectionClassName() {
                        return BadMetalogFactory.class.getName();
                    }
                }),
                Arguments.of(new Metalog.Config() {
                    @Override
                    public boolean useServiceLoader() {
                        return false;
                    }
                    @Override
                    public boolean useReflection() {
                        return true;
                    }
                    @Override
                    public String reflectionClassName() {
                        return "";
                    }
                })
            );
        }
    }
}

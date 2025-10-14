package io.github.jonloucks.metalog.test;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.Contracts;
import io.github.jonloucks.metalog.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.concurrent.atomic.AtomicReference;

import static io.github.jonloucks.contracts.test.Tools.assertInstantiateThrows;
import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static io.github.jonloucks.metalog.test.ConsoleTests.ConsoleTestsTools.assertIndirectConsole;
import static io.github.jonloucks.metalog.test.ConsoleTests.ConsoleTestsTools.assertDirectConsole;
import static io.github.jonloucks.metalog.test.Tools.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("CodeBlock2Expr")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public interface ConsoleTests {
    
    @Test
    default void console_output_WithNull_Throws() {
        withMetalog((contracts, metalog) -> {
            final Console console = contracts.claim(Console.CONTRACT);
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
             
                console.output(null);
           
            });
            assertThrown(thrown);
        });
    }
    
    @Test
    default void console_output_WithLog_Works() {
        withMetalog((contracts, metalog) -> {
            final Console console = contracts.claim(Console.CONTRACT);
            
            final Outcome outcome = console.output(() -> "Hello");
            
            assertOutcomeSuccess(outcome);
        });
    }
    
    @Test
    default void console_publish_WithLog_Works() {
        withMetalog((contracts, metalog) -> {
            final Console console = contracts.claim(Console.CONTRACT);
            
            final Outcome outcome = console.publish(() -> "Hello");
            
            assertOutcomeSuccess(outcome);
        });
    }
    
    @Test
    default void console_error_WithLog_Works() {
        withMetalog((contracts, metalog) -> {
            final Console console = contracts.claim(Console.CONTRACT);
            
            final Outcome outcome = console.error(() -> "Hello");
            
            assertOutcomeSuccess(outcome);
        });
    }
    
    @Test
    default void console_receive_WithNonConsoleLog_IsSkipped() {
        withMetalog((contracts, metalog) -> {
            final Console console = contracts.claim(Console.CONTRACT);
            
            final Outcome outcome = console.receive(() -> "xyz", Meta.DEFAULT);
            
            assertOutcomeSkipped(outcome);
        });
    }

    @Test
    default void console_error_WithNull_Throws() {
        withMetalog((contracts, metalog) -> {
            final Console console = contracts.claim(Console.CONTRACT);
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
           
                console.error(null);
           
            });
            assertThrown(thrown);
        });
    }
    
    @Test
    default void console_publish_WithNull_Throws() {
        withMetalog((contracts, metalog) -> {
            final Console console = contracts.claim(Console.CONTRACT);
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
          
                console.publish(null);
         
            });
            assertThrown(thrown);
        });
    }
    
    @ParameterizedTest(name = "channel = {0}")
    @ValueSource(strings = {"System.out", "System.err", "Console.output", "Console.error"})
    default void console_publish_Indirect_WithSupportedChannel(String channel) {
        withMetalog((contracts, metalog) -> {
      
            assertOutcomeSuccess(assertIndirectConsole(metalog, channel, 1));
       
        });
    }
    
    @ParameterizedTest(name = "channel = {0}")
    @ValueSource(strings = {"System.out", "System.err", "Console.output", "Console.error"})
    default void console_publish_AfterMetalogShutdown_Works(String channel) {
        final AtomicReference<Console> consoleRef = new AtomicReference<>();
        withMetalog((contracts, metalog) -> {
            consoleRef.set(contracts.claim(Console.CONTRACT));
        });
        
        assertNotNull(consoleRef.get().publish(() -> "Hello"));
    }
    
    @ParameterizedTest(name = "channel = {0}")
    @ValueSource(strings = {"System.out", "System.err", "Console.output", "Console.error"})
    default void console_publish_Indirect_WithSupportedChannel_Filtered(String channel) {
        withMetalog((contracts, metalog) -> {
            final Console console = contracts.claim(Console.CONTRACT);
            
            try (AutoClose anotherFilter = console.addFilter(m -> true);
                 AutoClose removeFilter = console.addFilter(x -> false)) {
                final AutoClose ignore1 = removeFilter, ignore2 = anotherFilter;
                
                assertOutcomeSkipped(assertIndirectConsole(metalog, channel, 0));
            }
            assertOutcomeSuccess(assertIndirectConsole(metalog, channel, 1));
        });
    }
    
    @ParameterizedTest(name = "channel = {0}")
    @ValueSource(strings = {"Console.error", "Console.output", "System.out", "System.err"})
    default void console_publish_Direct_WithSupportedChannel(String channel) {
        withMetalog((contracts, metalog) -> {
            
            assertOutcomeSuccess(assertDirectConsole(contracts, channel, 1));
            
        });
    }
    
    @ParameterizedTest(name = "channel = {0}")
    @ValueSource(strings = {"System.out", "System.err", "Console.output", "Console.error"})
    default void console_publish_Direct_WithSupportedChannel_Filtered(String channel) {
        withMetalog((contracts, metalog) -> {
            final Console console = contracts.claim(Console.CONTRACT);
            
            try (AutoClose anotherFilter = console.addFilter(m -> true);
                 AutoClose removeFilter = console.addFilter(x -> false)) {
                final AutoClose ignore1 = removeFilter, ignore2 = anotherFilter;
                
                assertOutcomeSkipped(assertDirectConsole(contracts, channel, 0));
            }
            assertOutcomeSuccess(assertDirectConsole(contracts, channel, 1));
        });
    }
    
    @ParameterizedTest(name = "channel = {0}")
    @ValueSource(strings = {"error", "info", "warn", "", "unknown"})
    default void console_publish_Direct_UnsupportedChannel(String channel) {
        withMetalog((contracts, metalog) -> {
            
            assertOutcomeSkipped(assertDirectConsole(contracts, channel, 0));
            
        });
    }
    
    @ParameterizedTest(name = "channel = {0}")
    @ValueSource(strings = {"error", "info", "warn", "", "unknown"})
    default void console_receive_WithUnsupportedChannel(String channel) {
        withMetalog((contracts, metalog) -> {
            
            assertOutcomeSkipped(assertIndirectConsole(metalog, channel, 0));
            
        });
    }
    
    @Test
    default void console_InternalCoverage() {
        assertInstantiateThrows(ConsoleTestsTools.class);
    }
    
    final class ConsoleTestsTools {
        private ConsoleTestsTools() {
            throw new IllegalStateException("Utility class.");
        }
   
        static Outcome assertIndirectConsole(Metalog metalog, String channel, int times) {
            final Log mockLog = mock(Log.class);
            when(mockLog.get()).thenReturn("hello");
            final Outcome outcome = metalog.publish(mockLog, b -> b.channel(channel).block());
           
            verify(mockLog, times(times)).get();
            return outcome;
        }
        
        static Outcome assertDirectConsole(Contracts contracts, String channel, int times) {
            final Console console = contracts.claim(Console.CONTRACT);
            final Log mockLog = mock(Log.class);
            when(mockLog.get()).thenReturn("hello");
            final Outcome outcome = console.publish(mockLog, b -> b.channel(channel).block());
            
            verify(mockLog, times(times)).get();
            
            return outcome;
        }
    }
}

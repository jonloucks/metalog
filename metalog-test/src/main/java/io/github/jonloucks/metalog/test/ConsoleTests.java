package io.github.jonloucks.metalog.test;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.Contracts;
import io.github.jonloucks.metalog.api.Console;
import io.github.jonloucks.metalog.api.Log;
import io.github.jonloucks.metalog.api.Metalog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static io.github.jonloucks.contracts.test.Tools.assertThrown;
import static io.github.jonloucks.metalog.test.ConsoleTests.ConsoleTestsTools.assertIndirectConsole;
import static io.github.jonloucks.metalog.test.ConsoleTests.ConsoleTestsTools.assertDirectConsole;
import static io.github.jonloucks.metalog.test.Tools.withMetalog;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SuppressWarnings("CodeBlock2Expr")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public interface ConsoleTests {
    
    @Test
    default void console_info_WithNull_Throws() {
        withMetalog((contracts, metalog) -> {
            final Console console = contracts.claim(Console.CONTRACT);
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                console.info(null);
            });
            assertThrown(thrown);
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
    @ValueSource(strings = {"System.out", "System.err", "Console.info", "Console.error"})
    default void console_publish_Indirect_WithSupportedChannel(String channel) {
        withMetalog((contracts, metalog) -> {
            assertIndirectConsole(metalog, channel, 1);
        });
    }
    
    @ParameterizedTest(name = "channel = {0}")
    @ValueSource(strings = {"System.out", "System.err", "Console.info", "Console.error"})
    default void console_publish_Indirect_WithSupportedChannel_Filtered(String channel) {
        withMetalog((contracts, metalog) -> {
            final Console console = contracts.claim(Console.CONTRACT);
            
            try (AutoClose removeFilter = console.addFilter(x -> false)) {
                final AutoClose ignoreRemoveFilter = removeFilter;
                assertIndirectConsole(metalog, channel, 0);
            }
            assertIndirectConsole(metalog, channel, 1);
        });
    }
    
    @ParameterizedTest(name = "channel = {0}")
    @ValueSource(strings = {"Console.error", "Console.info", "System.out", "System.err"})
    default void console_publish_Direct_WithSupportedChannel(String channel) {
        withMetalog((contracts, metalog) -> {
            assertDirectConsole(contracts, channel, 1);
        });
    }
    
    @ParameterizedTest(name = "channel = {0}")
    @ValueSource(strings = {"error", "info", "warn", "", "unknown"})
    default void console_publish_Direct_UnsupportedChannel(String channel) {
        withMetalog((contracts, metalog) -> {
            assertDirectConsole(contracts, channel, 0);
        });
    }
    
    @ParameterizedTest(name = "channel = {0}")
    @ValueSource(strings = {"error", "info", "warn", "", "unknown"})
    default void console_receive_WithUnsupportedChannel(String channel) {
        withMetalog((contracts, metalog) -> {
            assertIndirectConsole(metalog, channel, 0);
        });
    }
    
    final class ConsoleTestsTools {
        private ConsoleTestsTools() {
            throw new IllegalStateException("Utility class.");
        }
   
        static void assertIndirectConsole(Metalog metalog, String channel, int times) {
            final Log mockLog = mock(Log.class);
            when(mockLog.get()).thenReturn("hello");
            metalog.publish(mockLog, b -> b.channel(channel).block());
            verify(mockLog, times(times)).get();
        }
        
        static void assertDirectConsole(Contracts contracts, String channel, int times) {
            final Console console = contracts.claim(Console.CONTRACT);
            final Log mockLog = mock(Log.class);
            when(mockLog.get()).thenReturn("hello");
            console.publish(mockLog, b -> b.channel(channel).block());
            verify(mockLog, times(times)).get();
        }
    }
}

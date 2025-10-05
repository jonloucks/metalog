package io.github.jonloucks.metalog.test;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.metalog.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static io.github.jonloucks.contracts.test.Tools.assertInstantiateThrows;
import static io.github.jonloucks.contracts.test.Tools.assertObject;
import static io.github.jonloucks.metalog.test.Tools.metaWithId;
import static io.github.jonloucks.metalog.test.Tools.uniqueString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
}

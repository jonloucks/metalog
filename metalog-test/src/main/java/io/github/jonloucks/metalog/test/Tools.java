package io.github.jonloucks.metalog.test;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;

import io.github.jonloucks.metalog.api.Entity;
import io.github.jonloucks.metalog.api.Meta;
import io.github.jonloucks.metalog.api.MetalogFactory;
import io.github.jonloucks.metalog.api.Metalog;

import static io.github.jonloucks.contracts.test.Tools.sanitize;
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
    
    static Entity createTestEntity(String name, Object value) {
        return createTestEntity(name, value, null);
    }
    
    static Entity createTestEntity(String name, Object value, String id) {
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
            public Optional<Object> getValue() {
                return Optional.ofNullable(value);
            }
        };
    }

    /**
     * Utility class instantiation protection
     */
    private Tools() {
        throw new AssertionError("Illegal constructor");
    }
}

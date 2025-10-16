package io.github.jonloucks.metalog.test;

import io.github.jonloucks.metalog.api.Metalog.Config.Builder;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static io.github.jonloucks.metalog.api.Metalog.Config.DEFAULT;
import static io.github.jonloucks.metalog.test.Tools.withMetalog;
import static org.junit.jupiter.api.Assertions.assertEquals;

public interface MetalogConfigTests {
    
    @Test
    default void metalogConfig_Defaults() {
        withMetalog( (contracts,metalog)-> {
            final Builder builder = contracts.claim(Builder.FACTORY).get();
            
            assertEquals(DEFAULT.useReflection(), builder.useReflection());
            assertEquals(DEFAULT.useServiceLoader(), builder.useServiceLoader());
            assertEquals(DEFAULT.unkeyedFairness(), builder.unkeyedFairness());
            assertEquals(DEFAULT.contracts(), builder.contracts());
            assertEquals(DEFAULT.serviceLoaderClass(), builder.serviceLoaderClass());
            assertEquals(DEFAULT.keyedQueueLimit(), builder.keyedQueueLimit());
            assertEquals(DEFAULT.unkeyedThreadCount(), builder.unkeyedThreadCount());
            assertEquals(DEFAULT.reflectionClassName(), builder.reflectionClassName());
            assertEquals(DEFAULT.shutdownTimeout(), builder.shutdownTimeout());
        });
    }
    
    @Test
    default void metalogConfig_Modify() {
        withMetalog( (contracts,metalog)-> {
            final Builder builder = contracts.claim(Builder.FACTORY).get();
            
            builder
                .useReflection(!DEFAULT.useReflection())
                .useServiceLoader(!DEFAULT.useServiceLoader())
                .unkeyedFairness(!DEFAULT.unkeyedFairness())
                .contracts(contracts)
                .serviceLoaderClass(BadMetalogFactory.class)
                .keyedQueueLimit(DEFAULT.keyedQueueLimit()+1)
                .unkeyedThreadCount(DEFAULT.unkeyedThreadCount()+1)
                .reflectionClassName("MyReflectionClassName")
                .shutdownTimeout(DEFAULT.shutdownTimeout().plus(Duration.ofSeconds(1)));
            
            assertEquals(!DEFAULT.useReflection(), builder.useReflection());
            assertEquals(!DEFAULT.useServiceLoader(), builder.useServiceLoader());
            assertEquals(!DEFAULT.unkeyedFairness(), builder.unkeyedFairness());
            assertEquals(contracts, builder.contracts());
            assertEquals(BadMetalogFactory.class, builder.serviceLoaderClass());
            assertEquals(DEFAULT.keyedQueueLimit()+1, builder.keyedQueueLimit());
            assertEquals(DEFAULT.unkeyedThreadCount()+1, builder.unkeyedThreadCount());
            assertEquals("MyReflectionClassName", builder.reflectionClassName());
            assertEquals(DEFAULT.shutdownTimeout().plus(Duration.ofSeconds(1)), builder.shutdownTimeout());
        });
    }
}

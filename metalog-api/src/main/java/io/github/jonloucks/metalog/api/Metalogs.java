package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.*;

import java.time.Duration;

/**
 * The Metalogs service API
 */
public interface Metalogs extends Publisher, Filterable, AutoOpen {
    Contract<Metalogs> CONTRACT = Contract.create(Metalogs.class);
    
    AutoClose subscribe(Subscriber config);
    
    interface Config {
        Config DEFAULT = new Config() {
        };
        
        /**
         * @return if true, reflection might be used to locate the MetalogsFactory
         */
        default boolean useReflection() {
            return true;
        }
        
        /**
         * @return the class name to use if reflection is used to find the MetalogsFactory
         */
        default String reflectionClassName() {
            return "io.github.jonloucks.metalog.impl.ServiceFactoryImpl";
        }
        
        /**
         * @return if true, the ServiceLoader might be used to locate the MetalogsFactory
         */
        default boolean useServiceLoader() {
            return true;
        }
        
        /**
         * @return the class name to load from the ServiceLoader to find the MetalogsFactory
         */
        default Class<? extends MetalogsFactory> serviceLoaderClass() {
            return MetalogsFactory.class;
        }
        
        /**
         * @return the contracts service
         */
        default Contracts contracts() {
            return GlobalContracts.getInstance();
        }
        
        default boolean isEnabled() {
            return true;
        }
        
        default int backlogThreadCount() {
            return 1; // until sequenceKey
        }
        
        default Duration shutdownTimeout() {
            return Duration.ofSeconds(60);
        }
        
        default boolean systemOutput() {
            return true;
        }
    }
}

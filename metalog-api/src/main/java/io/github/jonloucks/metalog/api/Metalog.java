package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.*;

import java.time.Duration;

/**
 * The Metalog service API
 */
public interface Metalog extends Publisher, Filterable, AutoOpen {
    Contract<Metalog> CONTRACT = Contract.create(Metalog.class);
    
    AutoClose subscribe(Subscriber config);
    
    interface Config {
        Config DEFAULT = new Config() {
        };
        
        /**
         * @return if true, reflection might be used to locate the MetalogFactory
         */
        default boolean useReflection() {
            return true;
        }
        
        /**
         * @return the class name to use if reflection is used to find the MetalogFactory
         */
        default String reflectionClassName() {
            return "io.github.jonloucks.metalog.impl.ServiceFactoryImpl";
        }
        
        /**
         * @return if true, the ServiceLoader might be used to locate the MetalogFactory
         */
        default boolean useServiceLoader() {
            return true;
        }
        
        /**
         * @return the class name to load from the ServiceLoader to find the MetalogFactory
         */
        default Class<? extends MetalogFactory> serviceLoaderClass() {
            return MetalogFactory.class;
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

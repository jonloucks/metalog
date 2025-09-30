package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.ContractException;
import io.github.jonloucks.contracts.api.Contracts;

import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.ServiceLoader;

import static io.github.jonloucks.contracts.api.Checks.nullCheck;

final class MetalogFactoryFinder {
    private final Metalog.Config config;
    private final Contracts contracts;
    
    MetalogFactoryFinder(Metalog.Config config) {
        this.config = nullCheck(config, "config was null");
        this.contracts = config.contracts();
    }
    
    MetalogFactory find() {
        if (contracts.isBound(MetalogFactory.CONTRACT)) {
            return contracts.claim(MetalogFactory.CONTRACT);
        }
        
        return createByReflection()
            .or(this::createByServiceLoader)
            .orElseThrow(this::newNotFoundException);
    }
    
    private Optional<? extends MetalogFactory> createByServiceLoader() {
        if (config.useServiceLoader()) {
            try {
                final Class<? extends MetalogFactory> servicFactoryClass = nullCheck(config.serviceLoaderClass(), "config.serviceLoaderClass() was null");
                final ServiceLoader<? extends MetalogFactory> serviceLoader = ServiceLoader.load(servicFactoryClass);
                return serviceLoader.findFirst();
            } catch (Throwable thrown) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
    
    private Optional<MetalogFactory> createByReflection() {
        if (config.useReflection()) {
            final String className = nullCheck(config.reflectionClassName(), "config.reflectionClassName() was null");
            if (className.isEmpty()) {
                return Optional.empty();
            }
            try {
                final Class<?> bootstrapClass = Class.forName(className);
                final Constructor<?> bootstrapConstructor = bootstrapClass.getConstructor();
                return Optional.of((MetalogFactory) bootstrapConstructor.newInstance());
            } catch (Throwable thrown) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
    
    private ContractException newNotFoundException() {
        return new ContractException("Unable to find GlobalMetalog MetalogFactory");
    }
}

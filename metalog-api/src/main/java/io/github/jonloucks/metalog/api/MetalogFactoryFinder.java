package io.github.jonloucks.metalog.api;

import java.util.Optional;
import java.util.ServiceLoader;

import static io.github.jonloucks.contracts.api.Checks.configCheck;
import static io.github.jonloucks.contracts.api.Checks.nullCheck;
import static java.util.Optional.ofNullable;

/**
 * Responsible for locating and creating the MetalogFactory for a deployment.
 */
final class MetalogFactoryFinder {
    MetalogFactoryFinder(Metalog.Config config) {
        this.config = configCheck(config);
    }
    
    MetalogFactory find() {
        return createByReflection()
            .or(this::createByServiceLoader)
            .orElseThrow(this::newNotFoundException);
    }
    
    private Optional<? extends MetalogFactory> createByServiceLoader() {
        if (config.useServiceLoader()) {
            try {
                return ServiceLoader.load(getServiceFactoryClass()).findFirst();
            } catch (Throwable ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
    
    private Class<? extends MetalogFactory> getServiceFactoryClass() {
        return nullCheck(config.serviceLoaderClass(), "Metalog Service Loader class must be present.");
    }
    
    private Optional<MetalogFactory> createByReflection() {
        if (config.useReflection()) {
            return getReflectionClassName().map(this::createNewInstance);
        }
        return Optional.empty();
    }
    
    private MetalogFactory createNewInstance(String className) {
        try {
            return (MetalogFactory)Class.forName(className).getConstructor().newInstance();
        } catch (Throwable thrown) {
            return null;
        }
    }

    private Optional<String> getReflectionClassName() {
        return ofNullable(config.reflectionClassName()).filter(x -> !x.isEmpty());
    }
    
    private MetalogException newNotFoundException() {
        return new MetalogException("Unable to find Metalog factory.");
    }
    
    private final Metalog.Config config;
}

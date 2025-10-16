package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.contracts.api.Contracts;
import io.github.jonloucks.metalog.api.Metalog;
import io.github.jonloucks.metalog.api.MetalogFactory;

import java.time.Duration;

import static io.github.jonloucks.contracts.api.Checks.contractsCheck;
import static io.github.jonloucks.contracts.api.Checks.nullCheck;

final class ConfigBuilderImpl implements Metalog.Config.Builder {
    @Override
    public Builder useReflection(boolean useReflection) {
        this.useReflection = useReflection;
        return this;
    }
    
    @Override
    public Builder useServiceLoader(boolean useServiceLoader) {
        this.useServiceLoader = useServiceLoader;
        return this;
    }
    
    @Override
    public Builder contracts(Contracts contracts) {
        this.contracts = contractsCheck(contracts);
        return this;
    }
    
    @Override
    public Builder keyedQueueLimit(int keyedQueueLimit) {
        this.keyedQueueLimit = keyedQueueLimit;
        return this;
    }
    
    @Override
    public Builder unkeyedThreadCount(int unkeyedThreadCount) {
        this.unkeyedThreadCount = unkeyedThreadCount;
        return this;
    }
    
    @Override
    public Builder unkeyedFairness(boolean unkeyedFairness) {
        this.unkeyedFairness = unkeyedFairness;
        return this;
    }
    
    @Override
    public Builder shutdownTimeout(Duration shutdownTimeout) {
        this.shutdownTimeout = nullCheck(shutdownTimeout, "Shut down timeout must be present.");
        return this;
    }
 
    @Override
    public Builder reflectionClassName(String reflectionClassName) {
        this.reflectionClassName = nullCheck(reflectionClassName, "Reflection class name must be present.");
        return this;
    }
    
    @Override
    public Builder serviceLoaderClass(Class<? extends MetalogFactory> serviceLoaderClass) {
        this.serviceLoaderClass = nullCheck(serviceLoaderClass, "Service loader class must be present.");
        return this;
    }
    
    @Override
    public boolean useReflection() {
        return useReflection;
    }
    
    @Override
    public String reflectionClassName() {
        return reflectionClassName;
    }
    
    @Override
    public boolean useServiceLoader() {
        return useServiceLoader;
    }
    
    @Override
    public Class<? extends MetalogFactory> serviceLoaderClass() {
        return serviceLoaderClass;
    }
    
    @Override
    public Contracts contracts() {
        return contracts;
    }
    
    @Override
    public int keyedQueueLimit() {
        return keyedQueueLimit;
    }
    
    @Override
    public int unkeyedThreadCount() {
        return unkeyedThreadCount;
    }
    
    @Override
    public boolean unkeyedFairness() {
        return unkeyedFairness;
    }
    
    @Override
    public Duration shutdownTimeout() {
        return shutdownTimeout;
    }
 
    ConfigBuilderImpl() {
    
    }
    
    private boolean useReflection = DEFAULT.useReflection();
    private boolean useServiceLoader = DEFAULT.useServiceLoader();
    private Contracts contracts = DEFAULT.contracts();
    private int keyedQueueLimit = DEFAULT.keyedQueueLimit();
    private int unkeyedThreadCount = DEFAULT.unkeyedThreadCount();
    private boolean unkeyedFairness = DEFAULT.unkeyedFairness();
    private Duration shutdownTimeout = DEFAULT.shutdownTimeout();
    private String reflectionClassName = DEFAULT.reflectionClassName();
    private Class<? extends MetalogFactory> serviceLoaderClass = DEFAULT.serviceLoaderClass();
}

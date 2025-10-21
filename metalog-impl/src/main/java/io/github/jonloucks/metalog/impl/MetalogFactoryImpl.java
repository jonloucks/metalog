package io.github.jonloucks.metalog.impl;


import io.github.jonloucks.concurrency.api.*;
import io.github.jonloucks.contracts.api.*;
import io.github.jonloucks.metalog.api.*;

import java.util.Optional;
import java.util.function.Consumer;

import static io.github.jonloucks.concurrency.api.GlobalConcurrency.findConcurrencyFactory;
import static io.github.jonloucks.contracts.api.BindStrategy.IF_NOT_BOUND;
import static io.github.jonloucks.contracts.api.Checks.*;
import static io.github.jonloucks.contracts.api.GlobalContracts.lifeCycle;

/**
 * Creates Metalog instances
 * Opt-in construction via reflection, ServiceLoader or directly.
 */
public final class MetalogFactoryImpl implements MetalogFactory {
    
    /**
     * Publicly accessible constructor as an entry point into this library.
     * It can be invoked via reflection, ServiceLoader or directly.
     */
    public MetalogFactoryImpl() {
    }
    
    @Override
    public Metalog create(Metalog.Config config) {
        final Metalog.Config validConfig = enhancedConfigCheck(config);
        final Repository repository = validConfig.contracts().claim(Repository.FACTORY).get();
        
        installConcurrency(validConfig, repository);
        installCore(validConfig, repository);
        
        final MetalogImpl metalog = new MetalogImpl(validConfig, repository, true);
        repository.keep(Metalog.CONTRACT, () -> metalog);
        return metalog;
    }
    
    @Override
    public Metalog create(Consumer<Metalog.Config.Builder> builderConsumer) {
        final Consumer<Metalog.Config.Builder> validBuilderConsumer = builderConsumerCheck(builderConsumer);
        final ConfigBuilderImpl builder = new ConfigBuilderImpl();
        
        validBuilderConsumer.accept(builder);
        
        return create(builder);
    }
    
    @Override
    public void install(Metalog.Config config, Repository repository) {
        final Metalog.Config validConfig = enhancedConfigCheck(config);
        final Repository validRepository = nullCheck(repository, "Repository must be present.");
        
        installConcurrency(validConfig, validRepository);
        installCore(validConfig, validRepository);
        
        final Promisor<Metalog> metalogPromisor = lifeCycle(() -> new MetalogImpl(validConfig, validRepository, false));
        
        validRepository.keep(Metalog.CONTRACT, metalogPromisor, IF_NOT_BOUND);
    }
    
    private void installConcurrency(Metalog.Config config, Repository repository) {
        if (config.contracts().isBound(Concurrency.CONTRACT)) {
            return;
        }
        
        if (config.contracts().isBound(StateMachineFactory.CONTRACT)) {
            return;
        }
        final Concurrency.Config concurrencyConfig = new Concurrency.Config() {
            @Override
            public Contracts contracts() {
                return config.contracts();
            }
        };
        final Optional<ConcurrencyFactory> optionalFactory = findConcurrencyFactory(concurrencyConfig);
        
        optionalFactory.ifPresent(f -> f.install(concurrencyConfig, repository));
    }
    
    private Metalog.Config enhancedConfigCheck(Metalog.Config config) {
        final Metalog.Config candidateConfig = configCheck(config);
        final Contracts contracts = contractsCheck(candidateConfig.contracts());
        
        if (contracts.isBound(Metalog.CONTRACT)) {
            throw new MetalogException("Metalog is already bound.");
        }
        
        return candidateConfig;
    }
    
    private void installCore(Metalog.Config config, Repository repository) {
  
        repository.require(Repository.FACTORY);
        repository.require(WaitableFactory.CONTRACT);
        repository.require(StateMachineFactory.CONTRACT);
        
        repository.keep(Metalog.Config.Builder.FACTORY, () -> ConfigBuilderImpl::new, IF_NOT_BOUND);
        repository.keep(Entities.Builder.FACTORY, () -> EntitiesImpl::new, IF_NOT_BOUND);
        repository.keep(Entity.Builder.FACTORY, () -> EntityImpl::new, IF_NOT_BOUND);
        repository.keep(Meta.Builder.FACTORY, () -> MetaImpl::new, IF_NOT_BOUND);
        
        repository.keep(MetalogFactory.CONTRACT, lifeCycle(MetalogFactoryImpl::new), IF_NOT_BOUND);
        repository.keep(Dispatcher.KEYED_FACTORY, () -> () -> new KeyedDispatcherImpl(config), IF_NOT_BOUND);
        repository.keep(Dispatcher.UNKEYED_FACTORY, () -> ()-> new UnkeyedDispatcherImpl(config), IF_NOT_BOUND);
        repository.keep(Console.CONTRACT, lifeCycle(() -> new ConsoleImpl(config)), IF_NOT_BOUND);
    }
}

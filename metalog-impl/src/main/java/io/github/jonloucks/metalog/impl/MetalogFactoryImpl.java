package io.github.jonloucks.metalog.impl;


import io.github.jonloucks.concurrency.api.*;
import io.github.jonloucks.contracts.api.*;
import io.github.jonloucks.metalog.api.*;

import java.util.Optional;
import java.util.function.Consumer;

import static io.github.jonloucks.concurrency.api.GlobalConcurrency.findConcurrencyFactory;
import static io.github.jonloucks.contracts.api.BindStrategy.IF_ALLOWED;
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
        repository.keep(Metalog.CONTRACT, () -> metalog, IF_ALLOWED);
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
        
        validRepository.keep(Metalog.CONTRACT, metalogPromisor, IF_ALLOWED);
    }
    
    private void installConcurrency(Metalog.Config config, Repository repository) {
        final Concurrency.Config concurrencyConfig = new Concurrency.Config() {
            @Override
            public Contracts contracts() {
                return config.contracts();
            }
        };
        //noinspection ResultOfMethodCallIgnored
        contractsCheck(concurrencyConfig.contracts());
        final Optional<ConcurrencyFactory> optionalFactory = findConcurrencyFactory(concurrencyConfig);
        
        optionalFactory.ifPresent(f -> f.install(concurrencyConfig, repository));
    }
    
    private Metalog.Config enhancedConfigCheck(Metalog.Config config) {
        final Metalog.Config candidateConfig = configCheck(config);
        //noinspection ResultOfMethodCallIgnored
        contractsCheck(candidateConfig.contracts());
        return candidateConfig;
    }
    
    private void installCore(Metalog.Config config, Repository repository) {
        repository.require(Repository.FACTORY);
        repository.require(WaitableFactory.CONTRACT);
        repository.require(StateMachineFactory.CONTRACT);
        
        final BindStrategy strategy = IF_NOT_BOUND;
        
        repository.keep(Metalog.Config.Builder.FACTORY, () -> ConfigBuilderImpl::new, strategy);
        repository.keep(Entities.Builder.FACTORY, () -> EntitiesImpl::new, strategy);
        repository.keep(Entity.Builder.FACTORY, () -> EntityImpl::new, strategy);
        repository.keep(Meta.Builder.FACTORY, () -> MetaImpl::new, strategy);
        
        repository.keep(MetalogFactory.CONTRACT, lifeCycle(MetalogFactoryImpl::new), strategy);
        repository.keep(Dispatcher.KEYED_FACTORY, () -> () -> new KeyedDispatcherImpl(config), strategy);
        repository.keep(Dispatcher.UNKEYED_FACTORY, () -> ()-> new UnkeyedDispatcherImpl(config), strategy);
        repository.keep(MainDispatcher.CONTRACT, lifeCycle(() -> new MainDispatcherImpl(config, repository)), strategy);
        repository.keep(Console.CONTRACT, lifeCycle(() -> new ConsoleImpl(config)), strategy);
    }
}

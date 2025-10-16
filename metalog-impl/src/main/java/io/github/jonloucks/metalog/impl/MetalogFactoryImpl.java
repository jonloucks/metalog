package io.github.jonloucks.metalog.impl;


import io.github.jonloucks.contracts.api.*;
import io.github.jonloucks.metalog.api.*;

import java.util.function.Consumer;

import static io.github.jonloucks.contracts.api.BindStrategy.ALWAYS;
import static io.github.jonloucks.contracts.api.BindStrategy.IF_NOT_BOUND;
import static io.github.jonloucks.contracts.api.Checks.*;

/**
 * Creates Metalog instances
 * Opt-in construction via reflection or ServiceLoader
 */
public final class MetalogFactoryImpl implements MetalogFactory {
    public MetalogFactoryImpl() {
    }
    
    @Override
    public Metalog create(Metalog.Config config) {
        final Metalog.Config validConfig = enhancedConfigCheck(config);
        final Repository repository = validConfig.contracts().claim(Repository.FACTORY).get();
        
        installCore(validConfig, repository);
        
        final MetalogImpl metalog = new MetalogImpl(validConfig, repository);
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
        
        installCore(validConfig, validRepository);
        
        validRepository.keep(Metalog.CONTRACT, () -> new MetalogImpl(validConfig, validRepository), ALWAYS);
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
        final Promisors promisors = config.contracts().claim(Promisors.CONTRACT);
        
        repository.keep(Idempotent.FACTORY, () -> IdempotentImpl::new, IF_NOT_BOUND);
        repository.keep(Metalog.Config.Builder.FACTORY, () -> ConfigBuilderImpl::new, IF_NOT_BOUND);
        repository.keep(Entities.Builder.FACTORY, () -> EntitiesImpl::new, IF_NOT_BOUND);
        repository.keep(Entity.Builder.FACTORY, () -> EntityImpl::new, IF_NOT_BOUND);
        repository.keep(Meta.Builder.FACTORY, () -> MetaImpl::new, IF_NOT_BOUND);
        
        repository.keep(MetalogFactory.CONTRACT, promisors.createLifeCyclePromisor(MetalogFactoryImpl::new), IF_NOT_BOUND);
        repository.keep(Dispatcher.KEYED_FACTORY, () -> () -> new KeyedDispatcherImpl(config), IF_NOT_BOUND);
        repository.keep(Dispatcher.UNKEYED_FACTORY, () -> ()-> new UnkeyedDispatcherImpl(config), IF_NOT_BOUND);
        repository.keep(Console.CONTRACT, promisors.createLifeCyclePromisor(() -> new ConsoleImpl(config)), IF_NOT_BOUND);
    }
}

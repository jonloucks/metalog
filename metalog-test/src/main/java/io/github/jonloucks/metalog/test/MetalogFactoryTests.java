package io.github.jonloucks.metalog.test;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.contracts.api.Contracts;
import io.github.jonloucks.contracts.api.Repository;
import io.github.jonloucks.metalog.api.Metalog;
import io.github.jonloucks.metalog.api.MetalogException;
import io.github.jonloucks.metalog.api.MetalogFactory;
import org.junit.jupiter.api.Test;

import static io.github.jonloucks.contracts.test.Tools.*;
import static io.github.jonloucks.metalog.test.Tools.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("CodeBlock2Expr")
public interface MetalogFactoryTests {
    
    @Test
    default void metalogFactory_install_WithNullConfig_Throws() {
        withContracts(contracts -> {
            final Metalog.Config config = new Metalog.Config() {
                @Override
                public Contracts contracts() {
                    return contracts;
                }
            };
            final Repository repository = contracts.claim(Repository.FACTORY).get();
            final MetalogFactory metalogFactory = getMetalogFactory(config);
            
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                metalogFactory.install(null, repository);
            });
            
            assertThrown(thrown);
        });
    }
    
    @Test
    default void metalogFactory_install_WithNullRepository_Throws() {
        withContracts(contracts -> {
            final Metalog.Config config = new Metalog.Config() {
                @Override
                public Contracts contracts() {
                    return contracts;
                }
            };
            final MetalogFactory metalogFactory = getMetalogFactory(config);
            
            final IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                metalogFactory.install(config, null);
            });
            
            assertThrown(thrown);
        });
    }
    
    @Test
    default void metalogFactory_install_AlreadyBound_Throws() {
        withMetalog(b -> {}, (contracts, metalog)-> {
            final Metalog.Config config = new Metalog.Config() {
                @Override
                public Contracts contracts() {
                    return contracts;
                }
            };
            
            final Repository repository = contracts.claim(Repository.FACTORY).get();
            final MetalogFactory metalogFactory = getMetalogFactory(config);
            
            final MetalogException thrown = assertThrows(MetalogException.class, () -> {
                metalogFactory.install(config, repository);
            });
            
            assertThrown(thrown);
        });
    }
    
    @Test
    default void metalogFactory_install_WithValid_Works() {
        withContracts(contracts -> {
            final Metalog.Config config = new Metalog.Config() {
                @Override
                public Contracts contracts() {
                    return contracts;
                }
            };
            
            final Repository repository = contracts.claim(Repository.FACTORY).get();
            final MetalogFactory metalogFactory = getMetalogFactory(config);
            
            metalogFactory.install(config, repository);
            
            try (AutoClose closeRepository = repository.open()) {
                ignore(closeRepository);
                assertObject(contracts.claim(Metalog.CONTRACT));
            }
        });
    }
}

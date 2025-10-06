package io.github.jonloucks.metalog.impl;


import io.github.jonloucks.contracts.api.Contract;
import io.github.jonloucks.metalog.api.Meta;

import java.util.concurrent.Executor;

interface Dispatcher {
    Contract<Dispatcher> CONTRACT = Contract.create(Dispatcher.class);
    
    void dispatch(Meta meta, Runnable command);
}

package io.github.jonloucks.metalog.impl;


import io.github.jonloucks.contracts.api.Contract;

import java.util.concurrent.Executor;

interface Dispatcher extends Executor {
    Contract<Dispatcher> CONTRACT = Contract.create(Dispatcher.class);
}

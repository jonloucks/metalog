package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.Contract;

public interface SystemSubscriber extends Subscriber {
    Contract<SystemSubscriber> CONTRACT = Contract.create(SystemSubscriber.class);
}

package io.github.jonloucks.metalog.api;

@FunctionalInterface
public interface Subscriber {
    void receive(Log log, Meta meta);
}

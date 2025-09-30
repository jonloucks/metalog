package io.github.jonloucks.metalog.api;

import java.util.function.Supplier;

@FunctionalInterface
public interface Log extends Supplier<CharSequence> {
}

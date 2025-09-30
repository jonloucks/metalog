package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.AutoClose;

import java.util.function.Predicate;

public interface Filterable extends Predicate<Meta> {
    AutoClose addFilter(Predicate<Meta> filter);
}

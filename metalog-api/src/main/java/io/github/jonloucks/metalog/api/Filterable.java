package io.github.jonloucks.metalog.api;

import io.github.jonloucks.contracts.api.AutoClose;

import java.util.function.Predicate;

/**
 * Responsible for combining many predicates into one
 */
public interface Filterable extends Predicate<Meta> {
    
    /**
     * Add a new filter
     * @param filter the filter to add
     * @return close will remove the filter
     */
    AutoClose addFilter(Predicate<Meta> filter);
}

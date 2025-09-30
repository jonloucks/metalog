package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.contracts.api.AutoClose;
import io.github.jonloucks.metalog.api.Filterable;
import io.github.jonloucks.metalog.api.Meta;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import static io.github.jonloucks.metalog.impl.Internal.filterCheck;

final class FiltersImpl implements Filterable {
    
    @Override
    public AutoClose addFilter(Predicate<Meta> filter) {
        final Predicate<Meta> validFilter = filterCheck(filter);
        
        filters.add(validFilter);
        
        return ()-> filters.removeIf(x -> x == validFilter);
    }
    
    @Override
    public boolean test(Meta meta) {
        if (filters.isEmpty()) {
            return true;
        }
        return filters.stream().allMatch(filter -> filter.test(meta));
    }
    
    private final List<Predicate<Meta>> filters = new CopyOnWriteArrayList<>();
}

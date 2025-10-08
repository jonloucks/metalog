package io.github.jonloucks.metalog.api;

/**
 * Like an iterator except there is not a 'remove'
 * @param <T> the type of object visited
 */
@FunctionalInterface
public interface Visitor<T> {
    /**
     * Called for each item
     * @param t the current object being visited
     * @return false will cause the traversal to stop
     */
    boolean visit(T t);
}

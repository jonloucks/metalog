package io.github.jonloucks.metalog.api;

import java.util.function.Supplier;

/**
 * The invoked when the log message text is actually needed
 */
@FunctionalInterface
public interface Log extends Supplier<CharSequence> {
}

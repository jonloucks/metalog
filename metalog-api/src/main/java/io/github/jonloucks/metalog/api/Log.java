package io.github.jonloucks.metalog.api;

import java.util.function.Supplier;

/**
 * Responsible for producing the text for a log message.
 * It is invoked only when the message is actually being consumed.
 * It is only invoked once.
 * The text is a CharSequence to avoid requiring everything being a string
 * For example, a StringBuilder could be returned without having to call toString()
 */
@FunctionalInterface
public interface Log extends Supplier<CharSequence> {
    
    /**
     * @return the log message
     * Implementations can rely on this method being called at most one time
     */
    @Override
    CharSequence get();
}

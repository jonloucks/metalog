package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.metalog.api.Log;

import static io.github.jonloucks.metalog.impl.Internal.logCheck;

final class InvokeOnlyOnce implements Log {
    private final Log referent;
    private boolean firstTime = true;
    
    InvokeOnlyOnce(Log referent) {
        this.referent = logCheck(referent);
    }
    
    @Override
    public synchronized CharSequence get() {
        if (firstTime) {
            firstTime = false;
            cachedText = referent.get();
        }
        return cachedText;
    }
    
    private volatile CharSequence cachedText;
}

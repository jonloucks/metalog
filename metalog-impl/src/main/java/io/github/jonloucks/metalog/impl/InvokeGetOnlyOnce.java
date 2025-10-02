package io.github.jonloucks.metalog.impl;

import io.github.jonloucks.metalog.api.Log;

final class InvokeGetOnlyOnce implements Log {
    private final Log referent;
    
    InvokeGetOnlyOnce(Log referent) {
        this.referent = referent;
    }
    
    @Override
    public CharSequence get() {
        if (null == cachedText) {
            synchronized (this) {
                // double check
                if (null == cachedText) {
                    cachedText = referent.get();
                }
            }
        }
        return cachedText;
    }
    
    private volatile CharSequence cachedText;
}

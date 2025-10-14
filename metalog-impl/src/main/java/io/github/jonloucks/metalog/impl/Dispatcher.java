package io.github.jonloucks.metalog.impl;


import io.github.jonloucks.metalog.api.Meta;
import io.github.jonloucks.metalog.api.Outcome;

/**
 *
 */
interface Dispatcher {

    Outcome dispatch(Meta meta, Runnable command);
}

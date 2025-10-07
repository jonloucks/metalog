package io.github.jonloucks.metalog.impl;


import io.github.jonloucks.metalog.api.Meta;

interface Dispatcher {

    void dispatch(Meta meta, Runnable command);
}

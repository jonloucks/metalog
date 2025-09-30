package io.github.jonloucks.metalog.api;


import java.util.function.Consumer;


public interface Publisher  {
    
    default void publish(Log log) {
        publish(log, Meta.DEFAULT);
    }
    
    void publish(Log log, Meta meta);
 
    void publish(Log log, Consumer<Meta.Builder<?>> metaBuilder);
    
    default boolean isEnabled() {
        return true;
    }
}

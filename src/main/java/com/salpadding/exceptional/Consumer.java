package com.salpadding.exceptional;

public interface Consumer<T, E extends Exception> {
    void consume(T data) throws E;
}

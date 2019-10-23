package com.salpadding.exceptional;

public interface Consumer<T, E extends Throwable> {
    void consume(T data) throws Throwable;
}

package com.salpadding.monad;

public interface ExceptionalConsumer<T> {
    void consume(T data) throws Exception;
}

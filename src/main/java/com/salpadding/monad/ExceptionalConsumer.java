package com.salpadding.monad;

public interface ExceptionalConsumer<T> {
    void accept(T data) throws Exception;
}

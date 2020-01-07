package com.salpadding.monad;

@FunctionalInterface
public interface ExceptionalFunction<T, U> {
    U apply(T data) throws Exception;
}

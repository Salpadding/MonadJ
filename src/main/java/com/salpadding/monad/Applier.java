package com.salpadding.monad;

public interface Applier<T, U, E extends Exception> {
    U apply(T data) throws E;
}

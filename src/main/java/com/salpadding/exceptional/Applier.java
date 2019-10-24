package com.salpadding.exceptional;

public interface Applier<T, U, E extends Exception> {
    U apply(T data) throws E;
}

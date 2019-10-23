package com.salpadding.exceptional;

public interface Applier<T, U, E extends Throwable> {
    U apply(T data) throws E;
}

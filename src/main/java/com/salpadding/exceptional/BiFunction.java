package com.salpadding.exceptional;

public interface BiFunction<T, U, R, E extends Exception> {
    R apply(T t, U u) throws E;
}

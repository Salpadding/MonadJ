package com.salpadding.monad;

public interface Supplier<T, E extends Exception> {
    T get() throws E;
}

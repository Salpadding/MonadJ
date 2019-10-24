package com.salpadding.exceptional;

public interface Supplier<T, E extends Exception> {
    T get() throws E;
}

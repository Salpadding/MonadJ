package com.salpadding.exceptional;

public interface Supplier<T, E extends Throwable> {
    T get() throws E;
}

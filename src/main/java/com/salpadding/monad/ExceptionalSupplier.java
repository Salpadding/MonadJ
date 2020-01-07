package com.salpadding.monad;


@FunctionalInterface
public interface ExceptionalSupplier<T> {
    T get() throws Exception;
}

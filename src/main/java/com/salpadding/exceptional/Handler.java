package com.salpadding.exceptional;

public interface Handler<T, E extends Throwable, R, V extends Throwable> {
    R handle(T data, E error) throws V;
}

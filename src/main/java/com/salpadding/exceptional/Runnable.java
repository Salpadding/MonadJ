package com.salpadding.exceptional;

public interface Runnable<E extends Throwable>{
    void eval() throws E;
}
package com.salpadding.monad;

public interface Runnable<E extends Exception>{
    void eval() throws E;
}
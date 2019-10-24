package com.salpadding.exceptional;

public interface Runnable<E extends Exception>{
    void eval() throws E;
}
package com.salpadding.exceptional;

public interface Procedure<E extends Throwable>{
    void eval() throws E;
}
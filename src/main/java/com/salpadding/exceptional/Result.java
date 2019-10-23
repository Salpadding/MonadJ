package com.salpadding.exceptional;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * @param <T> Functor for functionally exception handling
 */
public class Result<T> {
    private T data;
    private Throwable error;
    private List<Procedure> procedures;

    private Result(T data, Throwable error) {
        this.data = data;
        this.error = error;
        this.procedures = new LinkedList<>();
    }

    private Result(T data, Throwable error, List<Procedure> procedures) {
        this.data = data;
        this.error = error;
        this.procedures = procedures;
    }

    /**
     *
     * @param data completed result, not Nullable
     * @param <U>  any type
     * @return data wrapper
     */
    public static <U> Result<U> of(U data) {
        if (data == null) {
            return new Result<>(null, new NullPointerException());
        }
        return new Result<>(data, null);
    }

    public static <U> Result<U> supply(Supplier<U, ? extends Throwable> supplier) {
        try {
            return new Result<>(supplier.get(), null);
        } catch (Throwable e) {
            return new Result<>(null, e);
        }
    }

    public <U> Result<U> map(Applier<T, U, ? extends Throwable> applier) {
        if (error != null) {
            return new Result<>(null, error, procedures);
        }
        try {
            return new Result<>(applier.apply(data), null, procedures);
        } catch (Throwable t) {
            return new Result<>(null, t, procedures);
        }
    }

    public Result<T> ifPresent(Consumer<T, ? extends Throwable> consumer) {
        if (error != null) {
            return this;
        }
        try {
            consumer.consume(data);
            return this;
        } catch (Throwable t) {
            return new Result<>(null, t, procedures);
        }
    }

    public <U> Result<U> flatMap(Function<T, Result<U>> function) {
        if (error != null) {
            return new Result<>(null, error, procedures);
        }
        Result<U> res = function.apply(data);
        List<Procedure> tmp = new LinkedList<>(procedures);
        tmp.addAll(res.procedures);
        res.procedures = tmp;
        return res;
    }

    public Result<T> except(java.util.function.Consumer<Throwable> consumer) {
        if (error != null) {
            consumer.accept(error);
            return this;
        }
        return this;
    }

    public Result<T> onClean(Consumer<T, ? extends Throwable> consumer) {
        this.procedures.add(() -> consumer.consume(data));
        return this;
    }

    public Result<T> cleanUp() {
        this.procedures.forEach(p -> {
            try {
                p.eval();
            } catch (Throwable ignored) {
            }
        });
        this.procedures = new LinkedList<>();
        return this;
    }

    public Result<T> orElse(T data) throws RuntimeException {
        if (data == null) {
            throw new NoSuchElementException("orElse require non null element");
        }
        if (error != null) {
            return new Result<>(data, null, procedures);
        }
        return this;
    }

    public T orElseGet(T data) {
        if (error != null) {
            return data;
        }
        return this.data;
    }

    public T get() throws RuntimeException {
        if (error != null) {
            throw new RuntimeException(error.getMessage());
        }
        return data;
    }

    public boolean isPresent() {
        return error == null;
    }
}

package com.salpadding.exceptional;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @param <T> Functor for functionally exception handling
 */
public class Result<T, E extends Throwable> {
    private T data;
    private E error;
    private List<Runnable> cleaners;

    private Result(T data, E error) {
        this.data = data;
        this.error = error;
        this.cleaners = new LinkedList<>();
    }

    private Result(T data, E error, List<Runnable> cleaners) {
        this.data = data;
        this.error = error;
        this.cleaners = cleaners;
    }

    /**
     * @param data completed result, not Nullable
     * @param <U>  any type
     * @return data wrapper
     */
    public static <U> Result<U, Throwable> of(U data) {
        if (data == null) {
            return new Result<>(null, new NullPointerException());
        }
        return new Result<>(data, null);
    }

    public static <U> Result<U, Throwable> supply(Supplier<U, ?> supplier) {
        return supply(supplier, e -> e);
    }

    public static <U, V extends Throwable> Result<U, V> supply(Supplier<U, ?> supplier, Function<Throwable, V> handler) {
        try {
            return new Result<>(supplier.get(), null);
        } catch (Throwable e) {
            return new Result<>(null, handler.apply(e));
        }
    }

    public <U> Result<U, Throwable> map(Applier<T, U, ? extends Throwable> applier) {
        return map(applier, e -> e);
    }

    public <U, V extends Throwable> Result<U, V> map(Applier<T, U, ?> applier, Function<Throwable, V> handler) {
        if (error != null) {
            return new Result<>(null, handler.apply(error), cleaners);
        }
        try {
            U u = applier.apply(data);
            if (u == null) {
                throw new NullPointerException();
            }
            return new Result<>(u, null, cleaners);
        } catch (Throwable t) {
            return new Result<>(null, handler.apply(t), cleaners);
        }
    }

    /**
     * @param consumer
     * @param handler  the handler never return null
     * @param <V>
     * @return
     */
    public <V extends Throwable> Result<T, V> ifPresent(Consumer<T, ? extends Throwable> consumer, Function<Throwable, V> handler) {
        if (error != null) {
            return new Result<>(null, handler.apply(error), cleaners);
        }
        try {
            consumer.consume(data);
            return new Result<>(data, null, cleaners);
        } catch (Throwable t) {
            return new Result<>(null, handler.apply(t), cleaners);
        }
    }

    public Result<T, Throwable> ifPresent(Consumer<T, ? extends Throwable> consumer) {
        return ifPresent(consumer, e -> e);
    }

    public <U> Result<U, Throwable> flatMap(Function<T, Result<U, ? extends Throwable>> function) {
        return flatMap(function, e -> e);
    }

    public <U, V extends Throwable> Result<U, V> flatMap(Function<T, Result<U, ? extends Throwable>> function, Function<Throwable, V> handler) {
        if (error != null) {
            return new Result<>(null, handler.apply(error), cleaners);
        }
        Result<U, ?> res = function.apply(data);
        try {
            if (res == null) {
                throw new NullPointerException("return null Result in flatMap");
            }
            List<Runnable> tmp = new LinkedList<>(cleaners);
            tmp.addAll(res.cleaners);
            return new Result<>(res.data, handler.apply(res.error), tmp);
        } catch (Throwable t) {
            return new Result<>(null, handler.apply(t), cleaners);
        }
    }

    /**
     * @param function handle exception in functional way
     * @return
     */
    public <V extends Throwable> Result<T, V> handle(Function<E, V> function) {
        if (error != null) {
            return new Result<>(null, function.apply(error), cleaners);
        }
        return new Result<>(data, null, cleaners);
    }

    public Result<T, E> except(java.util.function.Consumer<E> consumer) {
        return handle((e) -> {
            consumer.accept(e);
            return e;
        });
    }

    /**
     * @param consumer the clean up method of resource
     * @return
     */
    public Result<T, E> onClean(Consumer<T, ? extends Throwable> consumer) {
        this.cleaners.add(() -> consumer.consume(data));
        return this;
    }

    /**
     * @return invoke onClean function of every resource
     */
    public Result<T, E> cleanUp() {
        this.cleaners.forEach(p -> {
            try {
                p.eval();
            } catch (Throwable ignored) {
            }
        });
        this.cleaners = new LinkedList<>();
        return this;
    }

    /**
     * @param data complement value
     * @return data when error occurs
     */
    public T orElse(T data) {
        if (error != null) {
            return data;
        }
        return this.data;
    }

    /**
     * @param supplier provide the complement value
     * @return supplied value when error occurs
     */
    public T orElseGet(java.util.function.Supplier<T> supplier) {
        if (error != null) {
            return supplier.get();
        }
        return data;
    }

    /**
     * @return wrapped value
     * @throws E when error occurs
     */
    public T get() throws E {
        if (error != null) {
            throw error;
        }
        return data;
    }

    /**
     * @param handler handle exception when exception occurs
     * @param <V>
     * @return the wrapped value
     * @throws V
     */
    public <V extends Throwable> T get(Function<Throwable, V> handler) throws V {
        if (error != null) {
            throw handler.apply(error);
        }
        return data;
    }

    /**
     * @return returns when no exception occurs and no null value returns
     */
    public boolean isPresent() {
        return error == null;
    }
}

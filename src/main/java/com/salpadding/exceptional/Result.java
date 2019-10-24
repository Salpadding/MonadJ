package com.salpadding.exceptional;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * @param <T> Functor for functionally exception handling
 */
public class Result<T, E extends Exception> {
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


    public static <U> Result<U, Exception> of(U data) {
        return of(data, e -> e);
    }

    /**
     *
     * @param data nullable object
     * @param handler handle null exception when object is null
     */
    public static <U, V extends Exception> Result<U, V> of(U data, Function<Exception, V> handler) {
        Objects.requireNonNull(handler);
        try {
            return new Result<>(Objects.requireNonNull(data), null);
        } catch (Exception e) {
            return new Result<>(null, Objects.requireNonNull(handler.apply(e)));
        }
    }

    public static <U> Result<U, Exception> supply(Supplier<U, ? extends Exception> supplier) {
        return supply(supplier, e -> e);
    }

    public static <U, V extends Exception> Result<U, V> supply(Supplier<U, ? extends Exception> supplier,
                                                               Function<Exception, V> handler) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(handler);
        try {
            return new Result<>(Objects.requireNonNull(supplier.get()), null);
        } catch (Exception e) {
            return new Result<>(null, Objects.requireNonNull(handler.apply(e)));
        }
    }

    public <U> Result<U, Exception> map(Applier<T, U, ? extends Exception> applier) {
        return map(applier, e -> e);
    }

    public <U, V extends Exception> Result<U, V> map(Applier<T, U, ?> applier, Function<Exception, V> handler) {
        Objects.requireNonNull(handler);
        Objects.requireNonNull(applier);
        if (error != null) {
            return new Result<>(null, Objects.requireNonNull(handler.apply(error)), cleaners);
        }
        try {
            return new Result<>(Objects.requireNonNull(applier.apply(data)), null, cleaners);
        } catch (Exception e) {
            return new Result<>(null, Objects.requireNonNull(handler.apply(e)), cleaners);
        }
    }

    public <V extends Exception> Result<T, V> ifPresent(Consumer<T, ? extends Exception> consumer,
                                                        Function<Exception, V> handler) {
        Objects.requireNonNull(consumer);
        Objects.requireNonNull(handler);
        if (error != null) {
            return new Result<>(null, Objects.requireNonNull(handler.apply(error)), cleaners);
        }
        try {
            consumer.consume(data);
            return new Result<>(data, null, cleaners);
        } catch (Exception e) {
            return new Result<>(null, Objects.requireNonNull(handler.apply(e)), cleaners);
        }
    }

    public Result<T, Exception> ifPresent(Consumer<T, ? extends Exception> consumer) {
        return ifPresent(consumer, e -> e);
    }

    public <U> Result<U, Exception> flatMap(Function<T, Result<U, ? extends Exception>> function) {
        return flatMap(function, e -> e);
    }

    public <U, V> Result<V, Exception> compose(Result<U, ? extends Exception> other,
                                               BiFunction<T, U, V, ? extends Exception> function) {
        return compose(other, function, e -> e);
    }

    /**
     * M a -> M b -> ( a -> b -> c ) -> M c
     *
     * @param <U>
     * @return
     */
    public <U, V, R extends Exception> Result<V, R> compose(Result<U, ? extends Exception> other,
                                                            BiFunction<T, U, V, ? extends Exception> function, Function<Exception, R> handler) {
        Objects.requireNonNull(other);
        Objects.requireNonNull(function);
        Objects.requireNonNull(handler);
        if (error != null) {
            return new Result<>(null, Objects.requireNonNull(handler.apply(error)), cleaners);
        }
        List<Runnable> tmp = new LinkedList<>(cleaners);
        tmp.addAll(other.cleaners);
        if (other.error != null) {
            return new Result<>(null, Objects.requireNonNull(handler.apply(error)), tmp);
        }
        try {
            return new Result<>(Objects.requireNonNull(function.apply(data, other.data)), null, tmp);
        } catch (Exception e) {
            return new Result<>(null, Objects.requireNonNull(handler.apply(e)), tmp);
        }
    }

    /**
     * M a -> (a -> M b) -> M b
     *
     * @param function
     * @param handler
     * @param <U>
     * @param <V>
     * @return
     */
    public <U, V extends Exception> Result<U, V> flatMap(Function<T, Result<U, ? extends Exception>> function,
                                                         Function<Exception, V> handler) {
        Objects.requireNonNull(function);
        Objects.requireNonNull(handler);
        if (error != null) {
            return new Result<>(null, Objects.requireNonNull(handler.apply(error)), cleaners);
        }
        try {
            Result<U, ?> res = Objects.requireNonNull(function.apply(data));
            List<Runnable> tmp = new LinkedList<>(cleaners);
            tmp.addAll(res.cleaners);
            return new Result<>(res.data, null, tmp);
        } catch (Exception e) {
            return new Result<>(null, Objects.requireNonNull(handler.apply(e)), cleaners);
        }
    }

    /**
     * @param function handle exception in functional way
     * @return
     */
    public <V extends Exception> Result<T, V> handle(Function<? super E, V> function) {
        Objects.requireNonNull(function);
        if (error != null) {
            return new Result<>(null, Objects.requireNonNull(function.apply(error)), cleaners);
        }
        return new Result<>(data, null, cleaners);
    }

    public Result<T, E> except(java.util.function.Consumer<? super E> consumer) {
        Objects.requireNonNull(consumer);
        return handle((e) -> {
            consumer.accept(e);
            return e;
        });
    }

    /**
     * @param consumer the clean up method of resource
     * @return
     */
    public Result<T, E> onClean(Consumer<T, ? extends Exception> consumer) {
        Objects.requireNonNull(consumer);
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
            } catch (Exception ignored) {
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
        Objects.requireNonNull(data);
        if (error != null) {
            return data;
        }
        return this.data;
    }

    /**
     * @param supplier provide the complement value
     * @return supplied value when error occurs
     */
    public T orElseGet(java.util.function.Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier);
        if (error != null) {
            return Objects.requireNonNull(supplier.get());
        }
        return data;
    }

    public T get() throws E {
        if (error != null) {
            throw error;
        }
        return data;
    }


    public <V extends Exception> T get(Function<? super E, V> handler) throws V {
        Objects.requireNonNull(handler);
        if (error != null) {
            throw Objects.requireNonNull(handler.apply(error));
        }
        return data;
    }

    public boolean isPresent() {
        return error == null;
    }
}

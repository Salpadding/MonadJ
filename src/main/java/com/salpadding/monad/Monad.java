package com.salpadding.monad;

import java.io.Closeable;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @param <T> Functor for functionally exception handling
 */
public class Monad<T> {
    private Object data;
    private Exception error;
    private List<Closeable> cleaners = new LinkedList<>();

    private static final Monad<?> EMPTY = new Monad<>(null, new NoSuchElementException());

    private Monad(T data, Exception error) {
        this.data = data;
        if (this.data instanceof Closeable) {
            cleaners.add((Closeable) this.data);
        }
        this.error = error;
    }

    /**
     * @return an empty monad, which contains no such element exception
     */
    public static <U> Monad<U> empty() {
        return (Monad<U>) EMPTY;
    }

    /**
     * a -> M a
     *
     * @param data nullable object
     * @return an empty monad if data is null or else a presented monad
     */
    public static <U> Monad<U> of(U data) {
        if (data == null) return (Monad<U>) EMPTY;
        return new Monad<>(data, null);
    }

    /**
     * a -> M a
     *
     * @param supplier
     * @param <U>
     * @return
     */
    public static <U> Monad<U> supply(ExceptionalSupplier<U> supplier) {
        try {
            return of(supplier.get());
        } catch (Exception e) {
            return new Monad<>(null, e);
        }
    }

    /**
     * M a -> (a -> b) -> M b
     *
     * @param exceptionalFunction
     * @param <U>
     * @return
     */
    public <U> Monad<U> map(ExceptionalFunction<? super T, ? extends U> exceptionalFunction) {
        if (error != null) {
            return (Monad<U>) this;
        }
        try {
            this.data = exceptionalFunction.apply((T) data);
            if (this.data == null) this.error = new NoSuchElementException();
            if (this.data instanceof Cloneable) {
                cleaners.add((Closeable) this.data);
            }
        } catch (Exception e) {
            this.data = null;
            this.error = e;
        }
        return (Monad<U>) this;
    }

    /**
     * M a -> a -> M a
     *
     * @param consumer
     * @return
     */
    public Monad<T> peek(ExceptionalConsumer<? super T> consumer) {
        if (error != null) {
            return this;
        }
        try {
            consumer.accept((T) data);
            return this;
        } catch (Exception e) {
            this.data = null;
            this.error = e;
            return this;
        }
    }

    /**
     * M a -> (a -> M b) -> M b
     *
     * @param function
     * @return
     */
    public <U> Monad<U> flatMap(ExceptionalFunction<? super T, Monad<U>> function) {
        if (error != null) {
            return (Monad<U>) this;
        }
        try {
            Monad<U> res = function.apply((T) data);
            this.data = res.data;
            this.error = res.error;
            this.cleaners.addAll(res.cleaners);
            return (Monad<U>) this;
        } catch (Exception e) {
            this.data = null;
            this.error = e;
            return (Monad<U>) this;
        }
    }


    /**
     * @param consumer invoke when error occurs
     * @return self
     */
    public void except(java.util.function.Consumer<? super Exception> consumer) {
        if (error != null) {
            consumer.accept(error);
        }
        cleanUp();
    }

    /**
     * @return invoke onClean function of every resource
     */
    private void cleanUp() {
        this.cleaners.forEach(p -> {
            try {
                p.close();
            } catch (Exception ignored) {
            }
        });
        this.cleaners = new LinkedList<>();
    }

    /**
     * return value and clean resources
     *
     * @param other complement value
     * @return data when error occurs
     */
    public T orElse(T other) {
        cleanUp();
        if (this.error != null) {
            return other;
        }
        return (T) data;
    }

    /**
     * return value and clean resources
     *
     * @param other complement value
     * @return data when error occurs
     */
    public T orElse(T other, Consumer<Exception> consumer) {
        cleanUp();
        if (this.error != null) {
            consumer.accept(error);
            return other;
        }
        return (T) data;
    }


    /**
     * return value and clean resources
     *
     * @param supplier provide the complement value
     * @return supplied value when error occurs
     */
    public T orElseGet(java.util.function.Supplier<? extends T> supplier) {
        cleanUp();
        if (error != null) {
            return supplier.get();
        }
        return (T) data;
    }

    /**
     * return value and clean resources
     *
     * @return wrapped value
     * @throws RuntimeException if error occurs
     */
    public T get() throws RuntimeException {
        cleanUp();
        if (error != null) {
            throw new RuntimeException(error);
        }
        return (T) data;
    }


    /**
     * return value and clean resources
     *
     * @return wrapped value
     */
    public <X extends Throwable> T orElseThrow(Function<Exception, X> supplier) throws X {
        cleanUp();
        if (error != null) {
            throw supplier.apply(error);
        }
        return (T) data;
    }

    public Monad<T> filter(Predicate<? super T> predicate) {
        if (error != null) {
            return this;
        }
        if (predicate.test((T) data)) {
            return this;
        }
        this.data = null;
        this.error = new NoSuchElementException();
        return this;
    }

    public void ifPresent(Consumer<T> consumer){
        cleanUp();
        if(error != null) return;
        consumer.accept((T) data);
    }

    public boolean isPresent() {
        cleanUp();
        return data != null;
    }
}

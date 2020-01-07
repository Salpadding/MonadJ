package com.salpadding.monad;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.Closeable;
import java.io.IOException;

@RunWith(JUnit4.class)
public class MonadTest {

    private static class MonadTestException extends Exception {

    }

    @Test
    public void testEmpty() {
        assert !Monad.empty().isPresent();
    }

    @Test
    public void testOfSuccess() {
        assert Monad.of("success").isPresent();
    }

    @Test
    public void testOfFailed() {
        assert !Monad.of(null).isPresent();
    }

    @Test
    public void testSupplySuccess() {
        assert Monad.supply(() -> "abcdef").isPresent();
    }

    @Test
    public void testSupplyFailed() {
        assert !Monad.supply(() -> 1 / 0).isPresent();
    }

    @Test
    public void testExceptAs() {
        MonadTestException someException = null;
        try {
            Monad.of(null).orElseThrow(e -> new MonadTestException());
        } catch (MonadTestException e) {
            someException = e;
        }
        assert someException != null;
    }

    @Test
    public void testMapSuccess() throws Throwable {
        assert Monad.of(1).map(i -> i + 1).get() == 2;
    }

    @Test
    public void testMapFailed() {
        assert !Monad.<Integer>of(null).map(i -> i + 1).isPresent();
        assert !Monad.of(1).map(i -> i / 0).isPresent();
    }

    @Test
    public void testIfPresentSuccess() {
        boolean[] booleans = new boolean[1];
        assert Monad.of(1).peek((i) -> {
            booleans[0] = true;
        }).isPresent();
        assert booleans[0];
    }

    @Test
    public void testIfPresentFailed() {
        assert !Monad.<Integer>of(null).peek(x -> {
        }).isPresent();
        assert !Monad.of(1).peek((i) -> {
            throw new Exception("xxx");
        }).isPresent();
    }


    @Test
    public void testFlatMapSuccess() throws Throwable {
        Monad<Boolean> success = Monad.of(true);
        assert Monad.of(1).flatMap(i -> success).get();
    }

    @Test
    public void testFlatMapFailed() {
        Monad<Boolean> success = Monad.of(true);
        assert !Monad.of(null).flatMap(i -> success).isPresent();
        assert !Monad.of(1).flatMap(i -> {
            int j = i / 0;
            return Monad.of(j);
        }).isPresent();
    }

    @Test
    public void testExceptSuccess() {
        boolean[] booleans = new boolean[1];
        Monad.of(null).except((e) -> {
            booleans[0] = true;
        });
        assert booleans[0];
    }

    @Test
    public void testCleanUpOnFailed() {
        boolean[] booleans = new boolean[1];
        Monad.of(new Closeable() {
            @Override
            public void close() throws IOException {
                booleans[0] = true;
            }
        }).orElse(null);
        assert booleans[0];
    }

    @Test
    public void testCleanUpOnSuccess() {
        boolean[] booleans = new boolean[1];
        Monad.of(new Closeable() {
            @Override
            public void close() throws IOException {
                booleans[0] = true;
            }
        }).map(z -> 1).orElse(0);
        assert booleans[0];
    }

    @Test
    public void testCleanUpOnlyOnce() {
        int[] numbers = new int[1];
        Monad<Closeable> monad = Monad.of(new Closeable() {
            @Override
            public void close() throws IOException {
                numbers[0]++;
            }
        });
        monad.get();
        monad.get()
        ;
        assert numbers[0] == 1;
    }

    @Test
    public void testCleanUpAfterFlatMap() {
        boolean[] booleans = new boolean[2];
        Monad.of(new Closeable() {
            @Override
            public void close() throws IOException {
                booleans[0] = true;
            }
        }).flatMap(i -> Monad.of(new Closeable() {
                    @Override
                    public void close() throws IOException {
                        booleans[1] = true;
                    }
                }))
                .orElse(null);
        assert booleans[0];
        assert booleans[1];
    }

    @Test
    public void testNotCleanUpWhenFlatMapFailed() {
        boolean[] booleans = new boolean[2];
        Monad.of(new Closeable() {
            @Override
            public void close() throws IOException {
                booleans[0] = true;
            }
        }).map(x -> null).flatMap(i -> Monad.of(new Closeable() {
            @Override
            public void close() throws IOException {
                booleans[1] = true;
            }
        })).orElse(null);
        assert booleans[0];
        assert !booleans[1];
    }

    @Test
    public void testOrElseOf() throws Exception {
        assert Monad.<Integer>of(null)
                .orElse(100) == 100;
        assert Monad.<Integer>of(1).orElse(1000) == 1;
    }

    @Test
    public void testOrElseOnFailed() {
        int i = Monad.supply(() -> 1 / 0).orElse(1);
        assert i == 1;
    }

    @Test
    public void testOrElseOnSuccess() {
        int i = Monad.supply(() -> 1 * 0).orElse(1);
        assert i == 0;
    }

    @Test
    public void testOrElseGetOnFailed() {
        int i = Monad.supply(() -> 1 / 0).orElseGet(() -> 1);
        assert i == 1;
    }

    @Test
    public void testOrElseGetOnSuccess() {
        int i = Monad.supply(() -> 1 * 0).orElseGet(() -> 1);
        assert i == 0;
    }

    @Test
    public void testGet() throws Exception {
        assert Monad.of(1).get() == 1;
        assert Monad.of(1).orElseThrow(e -> e) == 1;
        Exception[] exceptions = new Exception[1];
        try {
            Monad.of(null).orElseThrow((e) -> new RuntimeException());
        } catch (Exception e) {
            assert e instanceof RuntimeException;
            exceptions[0] = e;
        }
        assert exceptions[0] != null;
    }

    @Test
    public void testOrElseThrow() throws Exception {
        Monad.of(1).orElseThrow(e -> new Exception());
        Exception[] exceptions = new Exception[1];
        try {
            Monad.of(null).orElseThrow(e -> new RuntimeException());
        } catch (Exception e) {
            assert e instanceof RuntimeException;
            exceptions[0] = e;
        }
        assert exceptions[0] != null;
    }

    @Test
    public void testFilterSuccess() {
        assert Monad.of(1).filter(x -> x > 0).isPresent();
    }

    @Test
    public void testFilterFailed() {
        assert !Monad.of(0).filter(x -> x > 0).isPresent();
        assert !Monad.<Integer>of(null).filter(x -> x > 0).isPresent();
    }

    @Test
    public void testOrElse() throws Exception {
        assert Monad.<Integer>of(0)
                .flatMap(x -> Monad.of(1)).get() == 1;
    }

}

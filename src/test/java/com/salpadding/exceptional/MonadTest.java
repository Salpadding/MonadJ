package com.salpadding.exceptional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MonadTest {

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
    public void testMapSuccess() throws Throwable{
        assert Monad.of(1).map(i -> i + 1).get() == 2;
    }

    @Test
    public void testMapFailed() {
        assert !Monad.of(1).map(i -> i / 0).isPresent();
    }

    @Test
    public void testIfPresentSuccess() {
        boolean[] booleans = new boolean[1];
        assert Monad.of(1).ifPresent((i) -> {
            booleans[0] = true;
        }).isPresent();
        assert booleans[0];
    }

    @Test
    public void testIfPresentFailed() {
        assert !Monad.of(1).ifPresent((i) -> {
            throw new Exception("xxx");
        }).isPresent();
    }

    @Test
    public void testFlatMapSuccess() throws Throwable {
        Monad<Boolean, Exception> success = Monad.of(true);
        assert Monad.of(1).flatMap(i -> success).get();
    }

    @Test
    public void testFlatMapFailed() {
        Monad<Boolean, Exception> success = Monad.of(true);
        assert !Monad.of(null).flatMap(i -> success).isPresent();
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
        Monad.of(null).onClean((o) -> {
            booleans[0] = true;
        }).cleanUp();
        assert booleans[0];
    }

    @Test
    public void testCleanUpOnSuccess() {
        boolean[] booleans = new boolean[1];
        Monad.of(1).onClean((o) -> {
            booleans[0] = true;
        }).cleanUp();
        assert booleans[0];
    }

    @Test
    public void testCleanUpAfterFlatMap() {
        boolean[] booleans = new boolean[2];
        Monad.of(1)
                .onClean((o) -> booleans[0] = true)
                .flatMap(i -> Monad.of(null).onClean((o) -> booleans[1] = true))
                .cleanUp();
        assert booleans[0];
        assert booleans[1];
    }

    @Test
    public void testNotCleanUpWhenFlatMapFailed() {
        boolean[] booleans = new boolean[2];
        Monad.of(null)
                .onClean((o) -> booleans[0] = true)
                .flatMap(i -> Monad.of(true).onClean((o) -> booleans[1] = true))
                .cleanUp();
        assert booleans[0];
        assert !booleans[1];
    }

    @Test
    public void testOrElseOnFailed() {
        int i = Monad.supply(() -> 1 / 0).orElse(1);
        assert i == 1;
    }

    @Test
    public void testOrElseOnSuccess()  {
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
}

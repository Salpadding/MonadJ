package com.salpadding.monad;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MonadTest {

    private static class MonadTestException extends Exception {

    }

    @Test
    public void testEmpty() {
        assert !Monad.empty(Integer.class).isPresent();
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
            Monad.of(null, Integer.class).exceptAs(e -> new MonadTestException()).get();
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
        assert !Monad.of(null, Integer.class).map(i -> i + 1).isPresent();
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
        assert !Monad.of(null, Integer.class).ifPresent(x -> {}).isPresent();
        assert !Monad.of(1).ifPresent((i) -> {
            throw new Exception("xxx");
        }).isPresent();
    }

    @Test
    public void testCompose() throws Exception {
        assert Monad.of(1)
                .compose(Monad.of(2), Integer::sum).get() == 3;
        assert !Monad.of(null, Integer.class)
                .compose(Monad.of(2), Integer::sum).isPresent();
        assert !Monad.of(1).compose(Monad.of(null), Integer::sum).isPresent();
        assert !Monad.of(1).compose(Monad.of(0), Integer::divideUnsigned).isPresent();
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
    public void testCleanUpOnlyOnce() {
        int[] numbers = new int[1];
        Monad.of(1)
                .onClean((o) -> numbers[0]++)
                .cleanUp().cleanUp();
        ;
        assert numbers[0] == 1;
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
    public void testOrElseOf() throws Exception {
        assert Monad.of(null, Integer.class)
                .orElseOf(100).get() == 100;
        assert Monad.of(1).orElseOf(1000).get() == 1;
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
        assert Monad.of(1).get(e -> e) == 1;
        Exception[] exceptions = new Exception[1];
        try {
            Monad.of(null).get((e) -> new RuntimeException());
        } catch (Exception e) {
            assert e instanceof RuntimeException;
            exceptions[0] = e;
        }
        assert exceptions[0] != null;
    }

    @Test
    public void testOrElseThrow() throws Exception {
        Monad.of(1).orElseThrow(new Exception());
        Exception[] exceptions = new Exception[1];
        try {
            Monad.of(null).orElseThrow(new RuntimeException());
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
        assert !Monad.of(null, Integer.class).filter(x -> x > 0).isPresent();
    }

    @Test
    public void testOrElse() throws Exception{
        boolean[] booleans = new boolean[1];
        assert Monad.of(null, Integer.class)
                .orElse(Monad.of(1)).get() == 1;
        assert Monad.of(1).orElse(Monad.of(1).onClean(x -> booleans[0] = true)).get() == 1;
        assert booleans[0];
    }

}

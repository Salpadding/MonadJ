package com.salpadding.exceptional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ResultTest {

    @Test
    public void testOfSuccess() {
        assert Result.of("success").isPresent();
    }

    @Test
    public void testOfFailed() {
        assert !Result.of(null).isPresent();
    }

    @Test
    public void testSupplySuccess() {
        assert Result.supply(() -> "abcdef").isPresent();
    }

    @Test
    public void testSupplyFailed() {
        assert !Result.supply(() -> 1 / 0).isPresent();
    }

    @Test
    public void testMapSuccess() throws Throwable{
        assert Result.of(1).map(i -> i + 1).get() == 2;
    }

    @Test
    public void testMapFailed() {
        assert !Result.of(1).map(i -> i / 0).isPresent();
    }

    @Test
    public void testIfPresentSuccess() {
        boolean[] booleans = new boolean[1];
        assert Result.of(1).ifPresent((i) -> {
            booleans[0] = true;
        }).isPresent();
        assert booleans[0];
    }

    @Test
    public void testIfPresentFailed() {
        assert !Result.of(1).ifPresent((i) -> {
            throw new Exception("xxx");
        }).isPresent();
    }

    @Test
    public void testFlatMapSuccess() throws Throwable {
        Result<Boolean, Throwable> success = Result.of(true);
        assert Result.of(1).flatMap(i -> success).get();
    }

    @Test
    public void testFlatMapFailed() {
        Result<Boolean, Throwable> success = Result.of(true);
        assert !Result.of(null).flatMap(i -> success).isPresent();
    }

    @Test
    public void testExceptSuccess() {
        boolean[] booleans = new boolean[1];
        Result.of(null).except((e) -> {
            booleans[0] = true;
        });
        assert booleans[0];
    }

    @Test
    public void testCleanUpOnFailed() {
        boolean[] booleans = new boolean[1];
        Result.of(null).onClean((o) -> {
            booleans[0] = true;
        }).cleanUp();
        assert booleans[0];
    }

    @Test
    public void testCleanUpOnSuccess() {
        boolean[] booleans = new boolean[1];
        Result.of(1).onClean((o) -> {
            booleans[0] = true;
        }).cleanUp();
        assert booleans[0];
    }

    @Test
    public void testCleanUpAfterFlatMap() {
        boolean[] booleans = new boolean[2];
        Result.of(1)
                .onClean((o) -> booleans[0] = true)
                .flatMap(i -> Result.of(null).onClean((o) -> booleans[1] = true))
                .cleanUp();
        assert booleans[0];
        assert booleans[1];
    }

    @Test
    public void testNotCleanUpWhenFlatMapFailed() {
        boolean[] booleans = new boolean[2];
        Result.of(null)
                .onClean((o) -> booleans[0] = true)
                .flatMap(i -> Result.of(true).onClean((o) -> booleans[1] = true))
                .cleanUp();
        assert booleans[0];
        assert !booleans[1];
    }

    @Test
    public void testOrElseOnFailed() {
        int i = Result.supply(() -> 1 / 0).orElse(1);
        assert i == 1;
    }

    @Test
    public void testOrElseOnSuccess()  {
        int i = Result.supply(() -> 1 * 0).orElse(1);
        assert i == 0;
    }

    @Test
    public void testOrElseGetOnFailed() {
        int i = Result.supply(() -> 1 / 0).orElseGet(() -> 1);
        assert i == 1;
    }

    @Test
    public void testOrElseGetOnSuccess() {
        int i = Result.supply(() -> 1 * 0).orElseGet(() -> 1);
        assert i == 0;
    }
}

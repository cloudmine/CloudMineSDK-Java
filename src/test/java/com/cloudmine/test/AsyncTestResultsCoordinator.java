package com.cloudmine.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 * CMUser: johnmccarthy
 * Date: 6/6/12, 3:25 PM
 */
public class AsyncTestResultsCoordinator {
    private static boolean ignoreOnFailure = false;
    private static final List<AssertionError> errors = new ArrayList<AssertionError>();
    private static final List<Throwable> onFailures = new ArrayList<Throwable>();
    private static CountDownLatch latch;
    public static final int TIMEOUT = 20;

    public static void add(AssertionError error) {
        errors.add(error);
    }

    public static void add(Throwable thrown) {
        onFailures.add(thrown);
    }

    public static void reset() {
        reset(1);
    }

    public static void reset(int numberOfCallbacks) {
        latch = new CountDownLatch(numberOfCallbacks);
        errors.clear();
        onFailures.clear();
    }

    public static void waitThenAssertTestResults() {
        waitThenAssertTestResults(TIMEOUT);
    }

    public static void waitThenAssertTestResults(int secondsToWait) {
        waitForTestResults(secondsToWait);
        assertAsyncTaskResult();
        reset();
    }



    public static void waitForTestResults() {
        waitForTestResults(TIMEOUT);
    }

    public static void waitForTestResults(int secondsToWait) {
        try {
            boolean timedOut = !latch.await(secondsToWait, TimeUnit.SECONDS);
            if(timedOut) {
                throw new RuntimeException("Timedout!");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted", e);
        }
    }

    public static void done() {
        latch.countDown();
    }

    public static void ignoreOnFailure(boolean newValue) {
        ignoreOnFailure = newValue;
    }

    public static void assertAsyncTaskResult() throws AssertionError {
        for(AssertionError error : errors) {
            throw error;
        }
        if(!ignoreOnFailure) {
            for(Throwable thrown : onFailures) {
                if(thrown != null) {
                    thrown.printStackTrace();
                    throw new AssertionError(thrown);
                }
            }
        }
    }
}

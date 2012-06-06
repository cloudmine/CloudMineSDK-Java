package com.cloudmine.test;

import com.cloudmine.api.rest.callbacks.CloudMineWebServiceCallback;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 6/6/12, 3:36 PM
 */
public class TestServiceCallback<T> extends CloudMineWebServiceCallback<T> {

    private CloudMineWebServiceCallback<T> callback;

    public static <T> TestServiceCallback<T> testCallback(CloudMineWebServiceCallback<T> callback) {
        return new TestServiceCallback<T>(callback);
    }
    public static <T> TestServiceCallback<T> testCallback(int numberOfCallbacks, CloudMineWebServiceCallback<T> callback) {
        return new TestServiceCallback<T>(callback);
    }

    public TestServiceCallback(CloudMineWebServiceCallback<T> callback) {
        super(callback.constructor());
        this.callback = callback;
    }

    @Override
    public void onCompletion(T response) {
        try {
            callback.onCompletion(response);
        } catch(AssertionError t) {
            AsyncTestResultsCoordinator.add(t);
        } finally {
            AsyncTestResultsCoordinator.done();
        }
    }

    @Override
    public void onFailure(Throwable thrown, String message) {
        try {
            callback.onFailure(thrown, message);
        } catch(AssertionError t) {
            AsyncTestResultsCoordinator.add(t);
        } finally {
            AsyncTestResultsCoordinator.done();
        }
    }
}

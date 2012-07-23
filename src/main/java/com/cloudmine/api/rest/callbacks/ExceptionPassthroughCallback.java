package com.cloudmine.api.rest.callbacks;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class ExceptionPassthroughCallback<T> implements Callback<T> {
    private final Callback parentCallback;
    /**
     * Passes any exceptions received by the given callback to this callback
     * @param callback the callback that will receive any onFailure message from this
     */
    public ExceptionPassthroughCallback(Callback callback) {
        parentCallback = callback;
    }

    @Override
    /**
     * we can't just call into parentCallback here cause it could easily be a different class, subclasses should
     * provide their own implementation
     */
    public void onCompletion(T response) {

    }

    @Override
    public void onFailure(Throwable thrown, String message) {
        parentCallback.onFailure(thrown, message);
    }
}

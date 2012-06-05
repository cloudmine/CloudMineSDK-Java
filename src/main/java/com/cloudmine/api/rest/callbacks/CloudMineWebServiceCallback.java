package com.cloudmine.api.rest.callbacks;

import com.loopj.android.http.ResponseConstructor;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/22/12, 6:12 PM
 */
public abstract class CloudMineWebServiceCallback<T> implements WebServiceCallback<T> {

    private final ResponseConstructor<T> constructor;

    public CloudMineWebServiceCallback(ResponseConstructor<T> constructor) {
        this.constructor = constructor;
    }

    /**
     * Only one the onCompletion methods should be overridden
     * @param response
     */
    @Override
    public void onCompletion(T response) {

    }

    @Override
    public void onFailure(Throwable thrown, String message) {

    }
}

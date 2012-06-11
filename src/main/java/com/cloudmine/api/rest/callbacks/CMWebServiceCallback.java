package com.cloudmine.api.rest.callbacks;


import com.cloudmine.api.rest.response.ResponseConstructor;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/22/12, 6:12 PM
 */
public abstract class CMWebServiceCallback<T> implements WebServiceCallback<T> {

    private final ResponseConstructor<T> constructor;

    public CMWebServiceCallback(ResponseConstructor<T> constructor) {
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

    public ResponseConstructor<T> constructor() {
        return constructor;
    }
}

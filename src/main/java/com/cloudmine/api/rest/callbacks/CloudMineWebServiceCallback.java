package com.cloudmine.api.rest.callbacks;

import com.cloudmine.api.rest.CloudMineResponse;
import org.apache.http.HttpResponse;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/22/12, 6:12 PM
 */
public abstract class CloudMineWebServiceCallback<T> implements WebServiceCallback {

    private final CloudMineResponse.ResponseConstructor<T> constructor;

    public CloudMineWebServiceCallback(CloudMineResponse.ResponseConstructor<T> constructor) {
        this.constructor = constructor;
    }

    /**
     * Only one of the onCompletion methods should be overridden
     * @param response
     */
    public void onCompletion(HttpResponse response) {
        onCompletion(constructor.construct(response));
    }

    /**
     * Only one the onCompletion methods should be overridden
     * @param response
     */
    public void onCompletion(T response) {

    }

    @Override
    public void onFailure(Throwable thrown, String message) {

    }
}

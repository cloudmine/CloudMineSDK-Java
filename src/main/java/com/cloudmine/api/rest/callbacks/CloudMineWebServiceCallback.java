package com.cloudmine.api.rest.callbacks;

import com.cloudmine.api.rest.CloudMineResponse;
import com.loopj.android.http.AsyncHttpResponseHandler;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/22/12, 6:12 PM
 */
public abstract class CloudMineWebServiceCallback<T extends CloudMineResponse> extends AsyncHttpResponseHandler<T> implements WebServiceCallback {

    private final CloudMineResponse.ResponseConstructor<T> constructor;

    public CloudMineWebServiceCallback(CloudMineResponse.ResponseConstructor<T> constructor) {
        super(constructor);
        this.constructor = constructor;
    }

    /**
     * @param response
     */
    public void onCompletion(T response) {

    }

    @Override
    public void onFailure(Throwable thrown, String message) {

    }
}

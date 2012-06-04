package com.cloudmine.api.rest.callbacks;

import com.cloudmine.api.rest.CloudMineResponse;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.HttpResponseConsumerConstructor;
import org.apache.http.HttpResponse;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/23/12, 3:51 PM
 */
public class AndroidWebServiceCallback extends AsyncHttpResponseHandler implements WebServiceCallback {

    private final WebServiceCallback callback;

    public AndroidWebServiceCallback() {
        this(WebServiceCallback.DO_NOTHING, CloudMineResponse.CONSTRUCTOR);
    }

    public AndroidWebServiceCallback(WebServiceCallback callback, HttpResponseConsumerConstructor responseConsumerConstructor) {
        super(responseConsumerConstructor);
        this.callback = callback;
    }

    @Override
    public void onCompletion(HttpResponse response) {
        callback.onCompletion(response);
    }

    @Override
    public void onFailure(Throwable error, String message) {
        callback.onFailure(error, message);
    }
}

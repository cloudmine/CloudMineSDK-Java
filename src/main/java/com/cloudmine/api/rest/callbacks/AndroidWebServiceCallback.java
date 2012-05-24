package com.cloudmine.api.rest.callbacks;

import com.loopj.android.http.AsyncHttpResponseHandler;
import org.apache.http.HttpResponse;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/23/12, 3:51 PM
 */
public class AndroidWebServiceCallback extends AsyncHttpResponseHandler implements WebServiceCallback {

    private final WebServiceCallback callback;

    public AndroidWebServiceCallback() {
        this(WebServiceCallback.DO_NOTHING);
    }

    public AndroidWebServiceCallback(WebServiceCallback callback) {
        this.callback = callback;
    }
    @Override
    public void onCompleted(HttpResponse response) {
        callback.onCompleted(response);
    }

    @Override
    public void onFailure(Throwable error, String message) {
        callback.onFailure(error, message);
    }
}

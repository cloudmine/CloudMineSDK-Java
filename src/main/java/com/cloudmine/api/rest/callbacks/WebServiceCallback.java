package com.cloudmine.api.rest.callbacks;

import org.apache.http.HttpResponse;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/22/12, 6:11 PM
 */
public interface WebServiceCallback {
    public static final WebServiceCallback DO_NOTHING = new WebServiceCallback() {
        @Override
        public void onCompleted(HttpResponse response) {

        }

        @Override
        public void onFailure(Throwable error, String message) {

        }
    };
    public void onCompleted(HttpResponse response);
    public void onFailure(Throwable error, String message);
}

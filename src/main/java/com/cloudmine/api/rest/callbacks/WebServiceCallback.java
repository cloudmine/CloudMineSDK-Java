package com.cloudmine.api.rest.callbacks;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/22/12, 6:11 PM
 */
public interface WebServiceCallback<T> {
    public static final WebServiceCallback DO_NOTHING = new WebServiceCallback() {

        @Override
        public void onFailure(Throwable error, String message) {

        }

        @Override
        public void onCompletion(Object response) {

        }
    };
    public void onCompletion(T response);
    public void onFailure(Throwable error, String message);
}

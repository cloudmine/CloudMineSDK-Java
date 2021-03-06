package com.cloudmine.api.rest.callbacks;


import com.cloudmine.api.rest.response.ResponseConstructor;

/**
 * Base class for all Callback classes
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public abstract class CMCallback<T> implements Callback<T> {

    public static <T> Callback<T> doNothing() {
        return new Callback() {
            private long startTime;
            @Override
            public void onFailure(Throwable error, String message) {

            }

            @Override
            public void setStartTime(long startTime) {
                this.startTime = startTime;
            }

            @Override
            public long getStartTime() {
                return startTime;
            }

            @Override
            public void onCompletion(Object response) {

            }

            @Override
            public String toString() {
                return "doNothing() callback";
            }
        };
    }
    private final ResponseConstructor<T> constructor;
    private long startTime;
    /**
     * Classes that extend this should have a noargs constructor that provides this constructor based on T
     * @param constructor a way of constructing T from an {@link org.apache.http.HttpResponse}
     */
    public CMCallback(ResponseConstructor<T> constructor) {
        this.constructor = constructor;
    }


    @Override
    public void onCompletion(T response) {

    }

    @Override
    public void onFailure(Throwable thrown, String message) {

    }

    public ResponseConstructor<T> constructor() {
        return constructor;
    }

    @Override
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }
}

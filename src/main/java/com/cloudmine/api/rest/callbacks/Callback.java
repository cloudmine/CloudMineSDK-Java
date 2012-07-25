package com.cloudmine.api.rest.callbacks;

/**
 * Base callback class. Consists of two methods - onCompletion which is always called, and
 * onFailure which is called if the response code is not between 200 and 299, or if an
 * exception is thrown during execution.
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public interface Callback<T> {

    /**
     * Called when a response is received from cloudmine. Will be called even if the request failed
     * @param response the response object from the server, created from an HttpResponse based on the constructor
     */
    public void onCompletion(T response);

    /**
     * Called when the response code is not between 200 and 299 inclusive.
     * @param error any exception that was thrown while trying to send the request
     * @param message an additional error message; will usually be blank
     */
    public void onFailure(Throwable error, String message);

    /**
     * Sets when the call was sent; you shouldn't be calling this
     * @param startTime
     */
    public void setStartTime(long startTime);

    /**
     * Get when the call was sent; this is used to check performance
     * @return
     */
    public long getStartTime();
}

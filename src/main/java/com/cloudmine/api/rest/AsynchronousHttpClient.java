package com.cloudmine.api.rest;

import com.cloudmine.api.rest.callbacks.Callback;
import com.cloudmine.api.rest.response.ResponseConstructor;
import org.apache.http.client.methods.HttpUriRequest;

/**
 * Defines the methods that must be implemented by any http client that will be used for asynchronous requests
 * to the CloudMine API
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public interface AsynchronousHttpClient {

    /**
     * Send the given request to the CloudMine API and call into callback on completion. Constructs the arguments for
     * Callback by using the given ResponseConstructor
     * @param command to be sent to the CloudMine API
     * @param callback will be called on completion or failure
     * @param constructor converts an HttpResponse to the type expected by callback
     * @param <T> the type expected by callback
     */
    public <T> void executeCommand(HttpUriRequest command, Callback<T> callback, ResponseConstructor<T> constructor);
}

package com.cloudmine.api.rest;

import com.cloudmine.api.rest.callbacks.WebServiceCallback;
import com.cloudmine.api.rest.response.ResponseConstructor;
import org.apache.http.client.methods.HttpUriRequest;

import java.util.concurrent.Future;

/**
 * Defines the methods that must be implemented by any http client that will be used for asynchronous requests
 * to the CloudMine API
 * Copyright CloudMine LLC
 */
public interface AsynchronousHttpClient {

    /**
     * Send the given request to the CloudMine API and call into callback on completion. Constructs the arguments for
     * WebServiceCallback by using the given ResponseConstructor
     * @param command to be sent to the CloudMine API
     * @param callback will be called on completion or failure
     * @param constructor converts an HttpResponse to the type expected by callback
     * @param <T> the type expected by callback
     * @return a Future that provides synchronous access to the results of a command.
     */
    public <T> Future<T> executeCommand(HttpUriRequest command, WebServiceCallback<T> callback, ResponseConstructor<T> constructor);
}

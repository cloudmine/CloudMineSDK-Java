package com.cloudmine.api;

import com.cloudmine.api.rest.AsynchronousHttpClient;
import com.cloudmine.api.rest.callbacks.Callback;
import com.cloudmine.api.rest.response.ResponseConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class SynchronousHttpClient implements AsynchronousHttpClient {
    private static final Logger LOG = LoggerFactory.getLogger(SynchronousHttpClient.class);
    private final HttpClient httpClient = new DefaultHttpClient();
    @Override
    public <T> void executeCommand(HttpUriRequest command, Callback<T> callback, ResponseConstructor<T> constructor) {
        try {
            HttpResponse response = httpClient.execute(command);
            callback.onCompletion(constructor.construct(response));
        } catch (Throwable throwable) {
            LOG.error("Exception thrown while executing http command", throwable);
            callback.onFailure(throwable, throwable.getMessage());
        }
    }
}

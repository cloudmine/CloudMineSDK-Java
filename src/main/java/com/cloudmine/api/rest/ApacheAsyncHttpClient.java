package com.cloudmine.api.rest;

import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.callbacks.Callback;
import com.cloudmine.api.rest.response.ResponseConstructor;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.reactor.IOReactorException;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class ApacheAsyncHttpClient implements AsynchronousHttpClient {

    private final HttpAsyncClient client;

    {
        try {
            client = new DefaultHttpAsyncClient();
        } catch (IOReactorException e) {
            throw new CreationException(e);
        }
    }

    @Override
    public <T> void executeCommand(HttpUriRequest command, Callback<T> callback, ResponseConstructor<T> constructor) {
        client.start();

    }
}

package com.cloudmine.api.rest;

import com.cloudmine.api.rest.callbacks.WebServiceCallback;
import com.cloudmine.api.rest.response.ResponseConstructor;
import org.apache.http.client.methods.HttpUriRequest;

import java.util.concurrent.Future;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/23/12, 2:55 PM
 */
public interface AsynchronousHttpClient {

    public <T> Future<T> executeCommand(HttpUriRequest command, WebServiceCallback<T> callback, ResponseConstructor<T> constructor);
}

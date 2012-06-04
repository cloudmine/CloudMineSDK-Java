package com.cloudmine.api.rest;

import com.cloudmine.api.rest.callbacks.WebServiceCallback;
import com.loopj.android.http.HttpResponseConsumerConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import java.util.concurrent.Future;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/23/12, 2:55 PM
 */
public interface AsynchronousHttpClient {

    public Future<HttpResponse> executeCommand(HttpUriRequest command, WebServiceCallback callback, HttpResponseConsumerConstructor constructor);
}

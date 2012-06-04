package com.cloudmine.api.rest;

import com.cloudmine.api.rest.callbacks.AndroidWebServiceCallback;
import com.cloudmine.api.rest.callbacks.WebServiceCallback;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.HttpResponseConsumerConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import java.util.concurrent.Future;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/23/12, 6:26 PM
 */
public class AndroidAsynchronousHttpClient extends AsyncHttpClient implements AsynchronousHttpClient{
    @Override
    public Future<HttpResponse> executeCommand(HttpUriRequest command, WebServiceCallback callback, HttpResponseConsumerConstructor responseConstructor) {
         execute(command, new AndroidWebServiceCallback(callback, responseConstructor));
        return null;
    }
}

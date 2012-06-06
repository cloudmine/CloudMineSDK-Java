package com.loopj.android.http;

import org.apache.http.HttpResponse;

import java.util.concurrent.Future;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 6/5/12, 3:54 PM
 */
public interface ResponseConstructor<T> {
    public T construct(HttpResponse response);
    public Future<T> constructFuture(Future<HttpResponse> futureResponse);
}

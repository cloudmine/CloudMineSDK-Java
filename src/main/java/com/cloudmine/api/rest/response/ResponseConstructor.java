package com.cloudmine.api.rest.response;

import com.cloudmine.api.exceptions.CreationException;
import org.apache.http.HttpResponse;

import java.util.concurrent.Future;

/**
 * Used to provide an interface to construct various types of response objects from an {@link HttpResponse}
 * There is probably no reason for you to be dealing with this
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public interface ResponseConstructor<T> {
    /**
     * Create a new T from an HttpResponse
     * @param response
     * @return
     * @throws CreationException if unable to populate all the required fields from the given HttpResponse
     */
    public T construct(HttpResponse response) throws CreationException;

    /**
     * Construct a Future that will contain a T
     * @param futureResponse
     * @return
     */
    public Future<T> constructFuture(Future<HttpResponse> futureResponse);
}

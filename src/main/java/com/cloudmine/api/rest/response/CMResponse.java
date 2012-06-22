package com.cloudmine.api.rest.response;

import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.exceptions.JsonConversionException;
import com.cloudmine.api.rest.Json;
import com.cloudmine.api.rest.JsonUtilities;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The base response returned by requests to the cloudmine web service. Consists of the JSON response,
 * if any, and the status code.
 * Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class CMResponse implements Json {

    private static final Logger LOG = LoggerFactory.getLogger(CMResponse.class);
    public static final ResponseConstructor<CMResponse> CONSTRUCTOR = new ResponseConstructor<CMResponse>() {
        public CMResponse construct(HttpResponse response) {
            return new CMResponse(response);
        }

        public Future<CMResponse> constructFuture(Future<HttpResponse> response) {
            return createFutureResponse(response);
        }
    };

    private static final int NO_RESPONSE_CODE = 204;

    public static Future<CMResponse> createFutureResponse(Future<HttpResponse> response) {
        return createFutureResponse(response, CONSTRUCTOR);
    }

    public static <T> Future<T> createFutureResponse(final Future<HttpResponse> response, final ResponseConstructor<T> constructor) {
        return new Future<T>() {
            T cachedResponse;
            @Override
            public boolean cancel(boolean b) {
                return response.cancel(b);
            }

            @Override
            public boolean isCancelled() {
                return response.isCancelled();
            }

            @Override
            public boolean isDone() {
                return response.isDone();
            }

            @Override
            public T get() throws InterruptedException, ExecutionException {
                if(cachedResponse == null) {
                    cachedResponse = constructor.construct(response.get());
                }
                return cachedResponse;
            }

            @Override
            public T get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
                if(cachedResponse == null) {
                    cachedResponse = constructor.construct(response.get(l, timeUnit));
                }
                return cachedResponse;
            }
        };
    }

    private final Map<String, Object> baseMap;
    private final int statusCode;

    /**
     * Construct a CMResponse from an {@link HttpResponse}. It is unlikely you should be calling this
     * directly
     * @param response a response to a request to the cloudmine RESTful service
     */
    public CMResponse(HttpResponse response)  {
        if(response != null &&
                response.getStatusLine() != null) {
            statusCode = response.getStatusLine().getStatusCode();
        } else {
            statusCode = NO_RESPONSE_CODE;
        }
        baseMap = extractResponseMap(response);
    }

    /**
     * Used by tests only
     * @param messageBody
     * @param statusCode
     */
    protected CMResponse(String messageBody, int statusCode) {
        Map<String, Object> tempNode;
        try {
            tempNode = JsonUtilities.jsonToMap(messageBody);
        } catch (JsonConversionException e) {
            LOG.error("Exception parsing message body: " + messageBody, e);
            tempNode = new HashMap<String, Object>();
        }
        baseMap = tempNode;
        this.statusCode = statusCode;
    }

    private Map<String, Object> extractResponseMap(HttpResponse response) {
        Map<String, Object> responseMap = null;
        if(response == null ||
                statusCode > 202 ||
                !response.getEntity().getContentType().getValue().contains("json")) {
            LOG.info("Received null, error, or none json response");
        }else {
            try {
                responseMap = JsonUtilities.jsonToMap(response.getEntity().getContent());
            } catch (IOException e) {
                LOG.error("Failed parsing response entity content: ", e);
            } catch (JsonConversionException e) {
                LOG.error("Failed converting response content to json", e);
            }
        }
        return responseMap == null ?
                new HashMap<String, Object>() :
                        responseMap;
    }

    /**
     * Get the HTTP status code returned by the request
     * @return 1xx is Informational, 2xx is success, 3xx is redirection, 4xx is client error, 5xx is server error. See
     * the CloudMine developer documentation for exactly what may be returned for specific requests
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Retrieves objects from the top level node. Will return null if the key does not exist
     * @param key the json key for the Object you want
     * @return an Object associated with the given key, or null if none exist. Possible types include
     * String, Boolean, Integer, Map<String, Object>
     */
    public Object getObject(String key) {
        if(baseMap == null) {
            return null;
        }
        return baseMap.get(key);
    }

    /**
     * Check whether the JSON returned by the request includes a specific top level key
     * @param key the top level key to check
     * @return
     */
    public boolean hasObject(String key) {
        return baseMap != null && baseMap.containsKey(key);
    }

    /**
     * Check if the response status code was any of the provided codes
     * @param statusCodes 0..N codes to check
     * @return true if the response status code equals any of the provided codes, false otherwise
     */
    public boolean was(int... statusCodes) {
        for(int statusCode : statusCodes){
            if(this.statusCode == statusCode)
                return true;
        }
        return false;
    }

    /**
     * True if we got a success HTTP response code (2xx)
     * @return true if the status code was between 199 and 300 exclusive
     */
    public boolean wasSuccess() {
        return 199 < statusCode && statusCode < 300;
    }

    protected boolean isNotEmpty(Map<String, Object> map) {
        return map != null && !map.isEmpty();
    }

    public String toString() {
        try {
            return asJson();
        } catch (JsonConversionException e) {
            return "Unable to convert json: " + e.getMessage();
        }
    }

    private String asJsonString;

    @Override
    public String asJson() throws JsonConversionException {
        if(asJsonString == null) {
            asJsonString = JsonUtilities.mapToJson(baseMap);
        }
        return asJsonString;
    }
}

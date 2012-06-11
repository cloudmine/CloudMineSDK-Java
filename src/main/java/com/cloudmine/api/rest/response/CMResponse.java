package com.cloudmine.api.rest.response;

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
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/16/12, 3:26 PM
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

    private final Map<String, Object> baseMap;
    private final int statusCode;

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

    public CMResponse(HttpResponse response)  {
        if(response != null &&
                response.getStatusLine() != null) {
            statusCode = response.getStatusLine().getStatusCode();
        } else {
            statusCode = NO_RESPONSE_CODE;
        }
        response.getStatusLine().getStatusCode();
        baseMap = extractResponseMap(response);
    }

    public CMResponse(String messageBody, int statusCode) {
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
            }
        }
        return responseMap == null ?
                new HashMap<String, Object>() :
                        responseMap;
    }

    public int getStatusCode() {
        return statusCode;
    }




    /**
     * Retrieves objects from the top level node. Will return null if the key does not exist
     * @param key
     * @return
     */
    public Object getObject(String key) {
        if(baseMap == null) {
            return null;
        }
        return baseMap.get(key);
    }



    public boolean hasObject(String key) {
        return baseMap != null && baseMap.containsKey(key);
    }

    public boolean was(int... statusCodes) {
        for(int statusCode : statusCodes){
            if(this.statusCode == statusCode)
                return true;
        }
        return false;
    }

    /**
     * True if we got a success HTTP response code (2xx)
     * @return
     */
    public boolean wasSuccess() {
        return 199 < statusCode && statusCode < 300;
    }

    protected boolean isNotEmpty(Map<String, Object> map) {
        return map != null && !map.isEmpty();
    }

    public String toString() {
        return asJson();
    }

    private String asJsonString;

    @Override
    public String asJson() {
        if(asJsonString == null) {
            asJsonString = JsonUtilities.mapToJson(baseMap);
        }
        return asJsonString;
    }
}

package com.cloudmine.api.rest.response;

import com.cloudmine.api.SimpleCMObject;
import com.cloudmine.api.exceptions.JsonConversionException;
import com.cloudmine.api.rest.Json;
import com.cloudmine.api.rest.JsonUtilities;
import com.loopj.android.http.ResponseConstructor;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/16/12, 3:26 PM
 */
public class CloudMineResponse implements Json {

    private static final Logger LOG = LoggerFactory.getLogger(CloudMineResponse.class);
    public static final ResponseConstructor<CloudMineResponse> CONSTRUCTOR = new ResponseConstructor<CloudMineResponse>() {
        public CloudMineResponse construct(HttpResponse response) {
            return new CloudMineResponse(response);
        }

        public Future<CloudMineResponse> constructFuture(Future<HttpResponse> response) {
            return createFutureResponse(response);
        }
    };

    private static final int NO_RESPONSE_CODE = 204;
    private static final String SUCCESS = "success";
    private static final String ERRORS = "errors";
    public static final String EMPTY_JSON = "{ }";

    private final Map<String, Object> baseMap;
    private final Map<String, Object> successResponse;
    private final Map<String, Object> errorResponse;
    private final int statusCode;

    public static Future<CloudMineResponse> createFutureResponse(Future<HttpResponse> response) {
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

    public CloudMineResponse(HttpResponse response)  {
        if(response != null &&
                response.getStatusLine() != null) {
            statusCode = response.getStatusLine().getStatusCode();
        } else {
            statusCode = NO_RESPONSE_CODE;
        }
        response.getStatusLine().getStatusCode();
        baseMap = extractResponseMap(response);
        successResponse = convertToMap(baseMap.get(SUCCESS));
        errorResponse = convertToMap(baseMap.get(ERRORS));
    }

    public CloudMineResponse(String messageBody, int statusCode) {
        Map<String, Object> tempNode;
        try {
            tempNode = JsonUtilities.jsonToMap(messageBody);
        } catch (JsonConversionException e) {
            LOG.error("Exception parsing message body: " + messageBody, e);
            tempNode = new HashMap<String, Object>();
        }
        baseMap = tempNode;
        successResponse = convertToMap(baseMap.get(SUCCESS));
        errorResponse = convertToMap(baseMap.get(ERRORS));
        this.statusCode = statusCode;
    }

    private Map<String, Object> convertToMap(Object object) {
        if(object instanceof Map) {
            return (Map<String, Object>)object;
        } else {
            return new HashMap<String, Object>();
        }
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

    public List<SimpleCMObject> getSuccessObjects() {
        if(successResponse == null || successResponse.isEmpty()) {
            LOG.error("Null or non empty successResponse, empty list returned for getSuccessObjects");
            return Collections.emptyList();
        }
        List<SimpleCMObject> successObjects = new ArrayList<SimpleCMObject>();


        for(Map.Entry<String, Object> successEntry : successResponse.entrySet()) {
            String successName = successEntry.getKey();
            Map<String, Object> successMap = convertToMap(successEntry.getValue());
            successObjects.add(new SimpleCMObject(successName, successMap));
        }
        return successObjects;
    }

    public Map<String, Object> successMap() {
        return new HashMap<String, Object>(successResponse);
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

    public boolean hasSuccessKey(String key) {
        return successResponse != null &&
                    successResponse.containsKey(key);
    }

    public boolean hasSuccess() {
        return mapHasContents(successResponse);
    }

    public boolean hasError() {
        return mapHasContents(errorResponse);
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

    private boolean mapHasContents(Map<String, Object> map) {
        return map != null && !map.isEmpty();
    }

    public String toString() {
        return asJson();
    }

    private String asJsonString;

    @Override
    public String asJson() {
        if(asJsonString == null) {
            asJsonString = baseMap == null ?
                    EMPTY_JSON :
                    JsonUtilities.mapToJson(baseMap);
        }
        return asJsonString;
    }
}

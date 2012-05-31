package com.cloudmine.api.rest;

import com.cloudmine.api.SimpleCMObject;
import com.cloudmine.api.exceptions.JsonConversionException;
import com.fasterxml.jackson.databind.JsonNode;
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

    private final JsonNode baseNode;
    private final JsonNode successResponse;
    private final JsonNode errorResponse;
    private final int statusCode;

    public static interface ResponseConstructor<T extends CloudMineResponse> {
        public T construct(HttpResponse response);
        public Future<T> constructFuture(Future<HttpResponse> futureResponse);
    }

    public static Future<CloudMineResponse> createFutureResponse(Future<HttpResponse> response) {
        return createFutureResponse(response, CONSTRUCTOR);
    }

    public static <T extends CloudMineResponse> Future<T> createFutureResponse(final Future<HttpResponse> response, final ResponseConstructor<T> constructor) {
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
        JsonNode responseNode = extractResponseNode(response);
        baseNode = responseNode;
        successResponse = responseNode.get(SUCCESS);
        errorResponse = responseNode.get(ERRORS); //TODO If we receive a null response is that an error?
    }

    public CloudMineResponse(String messageBody, int statusCode) {
        JsonNode tempNode;
        try {
            tempNode = JsonUtilities.getNode(messageBody);
        } catch (JsonConversionException e) {
            LOG.error("Exception parsing message body: " + messageBody, e);
            tempNode = JsonUtilities.getNodeFactory().nullNode();
        }
        baseNode = tempNode;
        successResponse = baseNode.get(SUCCESS);
        errorResponse = baseNode.get(ERRORS);
        this.statusCode = statusCode;
    }

    private JsonNode extractResponseNode(HttpResponse response) {
        JsonNode responseNode = null;
        if(response == null ||
                statusCode > 202 ||
                !response.getEntity().getContentType().getValue().contains("json")) {
            LOG.info("Received null, error, or none json response");
            responseNode = JsonUtilities.getNodeFactory().nullNode();
        }else {
            try {
                responseNode = JsonUtilities.getNode(response.getEntity().getContent());
            } catch (IOException e) {
                LOG.error("Failed parsing response entity content: ", e);
            }
        }
        return responseNode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    protected JsonNode getSuccessNode() {
        return successResponse;
    }

    public List<SimpleCMObject> getSuccessObjects() {
        if(successResponse == null ||
                !(successResponse.isObject() || successResponse.isArray())) {
            LOG.error("Null or non empty successResponse, empty list returned for getSuccessObjects");
            return Collections.emptyList();
        }
        List<SimpleCMObject> successObjects = new ArrayList<SimpleCMObject>();
        Iterator<Map.Entry<String, JsonNode>> successFields = successResponse.fields();

        while(successFields.hasNext()) {
            Map.Entry<String, JsonNode> nodeEntry = successFields.next();
            Map<String, Object> contents = JsonUtilities.jsonToMap(nodeEntry.getValue().toString());
            String topLevelKey = nodeEntry.getKey();
            successObjects.add(new SimpleCMObject(topLevelKey, contents));
        }
        return successObjects;
    }

    public boolean successHasKey(String key) {
        return successResponse != null &&
                    successResponse.has(key);
    }

    public boolean hasSuccess() {
        return jsonNodeHasContents(successResponse);
    }

    public boolean hasError() {
        return jsonNodeHasContents(errorResponse);
    }

    public boolean hasNode(String key) {
        return baseNode != null && baseNode.has(key);
    }

    public JsonNode getNode(String key) {
        return baseNode == null ?
                    null :
                        baseNode.get(key);
    }

    public boolean was(int statusCode) {
        return this.statusCode == statusCode;
    }

    private boolean jsonNodeHasContents(JsonNode node) {
        return node != null && node.iterator().hasNext();
    }

    public String toString() {
        return baseNode == null ?
                super.toString() :
                    baseNode.toString();
    }

    @Override
    public String asJson() {
        return baseNode == null ?
                EMPTY_JSON :
                    baseNode.toString();
    }
}

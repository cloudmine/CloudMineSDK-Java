package com.cloudmine.api.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
    };

    private static final int NO_RESPONSE_CODE = 204;
    private static final String SUCCESS = "success";
    private static final String ERRORS = "errors";
    public static final String EMPTY_JSON = "{ }";

    private final JsonNode baseNode;
    private final JsonNode successResponse;
    private final JsonNode errorResponse;
    private final int statusCode;

    protected static abstract class ResponseConstructor<T extends CloudMineResponse> {
        public abstract T construct(HttpResponse response);
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

    private JsonNode extractResponseNode(HttpResponse response) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseNode = null;
        if(response == null ||
                statusCode > 202 ||
                !response.getEntity().getContentType().getValue().contains("json")) {
            LOG.info("Received null, error, or none json response");
            responseNode = mapper.getNodeFactory().nullNode();
        }else {
            try {
                responseNode = mapper.readValue(response.getEntity().getContent(), JsonNode.class);
            } catch (IOException e) {
                LOG.error("Failed parsing response entity content: ", e);
            }
        }
        return responseNode;
    }

    public int getStatusCode() {
        return statusCode;
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

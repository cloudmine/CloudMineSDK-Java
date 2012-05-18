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
public class CloudMineResponse {
    private static final Logger LOG = LoggerFactory.getLogger(CloudMineResponse.class);
    private static final String SUCCESS = "success";
    private static final String ERRORS = "errors";

    private final JsonNode baseNode;
    private final JsonNode successResponse;
    private final JsonNode errorResponse;

    public CloudMineResponse(HttpResponse response) {
        JsonNode responseNode = extractResponseNode(response);
        baseNode = responseNode;
        successResponse = responseNode.get(SUCCESS);
        errorResponse = responseNode.get(ERRORS); //TODO If we receive a null response is that an error?
    }

    private JsonNode extractResponseNode(HttpResponse response) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseNode = null;
        if(response == null ||
                response.getStatusLine().getStatusCode() > 202 ||
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

    private boolean jsonNodeHasContents(JsonNode node) {
        return node != null && node.iterator().hasNext();
    }

    public String toString() {
        return baseNode.toString();
    }
}

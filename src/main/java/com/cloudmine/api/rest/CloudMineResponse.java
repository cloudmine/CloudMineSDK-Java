package com.cloudmine.api.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;

import java.io.IOException;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/16/12, 3:26 PM
 */
public class CloudMineResponse {
    private static final String SUCCESS = "success";
    private static final String ERRORS = "errors";

    private final JsonNode baseNode;
    private final JsonNode successResponse;
    private final JsonNode errorResponse;

    public CloudMineResponse(HttpResponse response) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseNode = null;
        try {
            responseNode = mapper.readValue(response.getEntity().getContent(), JsonNode.class); //TODO error handling
        } catch (IOException e) {
            e.printStackTrace();;
            //TODO
        }
        baseNode = responseNode;
        successResponse = responseNode.get(SUCCESS);
        errorResponse = responseNode.get(ERRORS);

    }

    public boolean successHasKey(String key) {
        return successResponse != null &&
                    successResponse.has(key);
    }

    public boolean hasError() {
        return errorResponse != null && errorResponse.iterator().hasNext();
    }

    public String toString() {
        return baseNode.toString();
    }
}

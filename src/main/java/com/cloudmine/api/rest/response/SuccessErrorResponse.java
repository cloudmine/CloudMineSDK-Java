package com.cloudmine.api.rest.response;

import com.cloudmine.api.SimpleCMObject;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 6/7/12, 6:08 PM
 */
public class SuccessErrorResponse extends CMResponse {
    private static final Logger LOG = LoggerFactory.getLogger(SuccessErrorResponse.class);
    private static final String SUCCESS = "success";
    private static final String ERRORS = "errors";
    private final Map<String, Object> successResponse;
    private final Map<String, Object> errorResponse;
    public SuccessErrorResponse(HttpResponse response) {
        super(response);
        successResponse = convertToMap(getObject(SUCCESS));
        errorResponse = convertToMap(getObject(ERRORS));
    }

    public SuccessErrorResponse(String msgBody, int statusCode) {
        super(msgBody, statusCode);
        successResponse = convertToMap(getObject(SUCCESS));
        errorResponse = convertToMap(getObject(ERRORS));
    }

    private Map<String, Object> convertToMap(Object object) {
        if(object instanceof Map) {
            return (Map<String, Object>)object;
        } else {
            LOG.info("Converting a non Map object to an empty map: " + object);
            return new HashMap<String, Object>();
        }
    }

    public Map<String, Object> successMap() {
        return new HashMap<String, Object>(successResponse);
    }

    public boolean hasSuccess() {
        return isNotEmpty(successResponse);
    }

    public boolean hasError() {
        return isNotEmpty(errorResponse);
    }

    public boolean hasSuccessKey(String key) {
        return successResponse != null &&
                successResponse.containsKey(key);
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
}

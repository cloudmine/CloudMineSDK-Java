package com.cloudmine.api.rest.response;

import com.cloudmine.api.SimpleCMObject;
import com.cloudmine.api.exceptions.CreationException;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Parent class for CMResponses that include a "success" and "errors" top level key mapped to json
 * objects.
 * Copyright CloudMine LLC
 */
public class SuccessErrorResponse extends CMResponse {
    private static final Logger LOG = LoggerFactory.getLogger(SuccessErrorResponse.class);
    private static final String SUCCESS = "success";
    private static final String ERRORS = "errors";
    private final Map<String, Object> successResponse;
    private final Map<String, Object> errorResponse;

    /**
     * Instantiate a new SuccessErrorResponse. You probably shouldn't be calling this yourself
     * @param response returned from a call to the CloudMine API
     */
    public SuccessErrorResponse(HttpResponse response) {
        super(response);
        successResponse = convertToMap(getObject(SUCCESS));
        errorResponse = convertToMap(getObject(ERRORS));
    }

    protected SuccessErrorResponse(String msgBody, int statusCode) {
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

    /**
     * Returns a copy of the success json object, represented as a Map<String, Object></String,>
     * @return a copy of the success json object, represented as a Map<String, Object></String,>
     */
    public Map<String, Object> successMap() {
        return new HashMap<String, Object>(successResponse);
    }

    /**
     * Check whether any objects exist in the success response
     * @return true if the success response json object was not empty; false otherwise
     */
    public boolean hasSuccess() {
        return isNotEmpty(successResponse);
    }

    /**
     * Check whether any objects exist in the errors response
     * @return true if the errors response json object was not empty; false otherwise.
     */
    public boolean hasError() {
        return isNotEmpty(errorResponse);
    }

    /**
     * Check whether the success response json object contains the specific key
     * @param key the key to check
     * @return true if the success response has the given key at the top level
     */
    public boolean hasSuccessKey(String key) {
        return successResponse != null &&
                successResponse.containsKey(key);
    }

    /**
     * Get the error response json objects as a collection of SimpleCMObjects
     * @return the error response as a collection of SimpleCMObjects
     * @throws CreationException if the error response contained improperly formed json
     */
    public List<SimpleCMObject> errorObjects() throws CreationException {
        return getObjects(errorResponse);
    }

    /**
     * Get the success response json objects as a collection of SimpleCMObjects
     * @return the success response as a collection of SimpleCMObjects
     * @throws CreationException if the success response contained improperly formed json
     */
    public List<SimpleCMObject> successObjects() throws CreationException {
        return getObjects(successResponse);
    }

    private List<SimpleCMObject> getObjects(Map<String, Object> objectMap) throws CreationException {
        if(objectMap == null || objectMap.isEmpty()) {
            LOG.info("Null or non empty response object, empty list returned for getObjects");
            return Collections.emptyList();
        }
        List<SimpleCMObject> successObjects = new ArrayList<SimpleCMObject>();
        for(Map.Entry<String, Object> successEntry : objectMap.entrySet()) {
            String successName = successEntry.getKey();
            Map<String, Object> successMap = convertToMap(successEntry.getValue());
            successObjects.add(SimpleCMObject.SimpleCMObject(successName, successMap));
        }
        return successObjects;

    }
}

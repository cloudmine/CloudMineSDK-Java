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

/**
 * The base class for different types of responses.
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class ResponseBase<CODE> implements Json {
    private static final Logger LOG = LoggerFactory.getLogger(ResponseBase.class);
    private static final int NO_RESPONSE_CODE = 204;

    private final Map<String, Object> baseMap;
    private final int statusCode;


    public static final ResponseConstructor<ResponseBase> CONSTRUCTOR = new ResponseConstructor<ResponseBase>() {
        public ResponseBase construct(HttpResponse response) {
            return new CMResponse(response);
        }
    };

    protected ResponseBase(HttpResponse response)  {
        if(response != null &&
                response.getStatusLine() != null) {
            statusCode = response.getStatusLine().getStatusCode();
        } else {
            statusCode = NO_RESPONSE_CODE;
        }
        baseMap = extractResponseMap(response);
    }

    protected ResponseBase(String messageBody, int statusCode) {
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


    protected Map<String, Object> extractResponseMap(HttpResponse response) {
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
     * Get the response value for this request as an enum
     * @return the response value for this request as an enum
     */
    public CODE getResponseCode() {
        return null;
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
//        if(asJsonString == null) {
//            asJsonString = JsonUtilities.mapToJson(baseMap);
//        }
//        return asJsonString;
        return JsonUtilities.mapToJson(baseMap);
    }
}

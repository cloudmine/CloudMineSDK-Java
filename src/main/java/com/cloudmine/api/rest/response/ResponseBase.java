package com.cloudmine.api.rest.response;

import com.cloudmine.api.exceptions.ConversionException;
import com.cloudmine.api.rest.Transportable;
import com.cloudmine.api.rest.JsonUtilities;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * The base class for different types of responses.
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public abstract class ResponseBase<CODE> implements Transportable {
    private static final Logger LOG = LoggerFactory.getLogger(ResponseBase.class);
    private static final int NO_RESPONSE_CODE = 204;

    private final Map<String, Object> baseMap;
    private final int statusCode;
    private final String messageBody;


    public static final ResponseConstructor<ResponseBase> CONSTRUCTOR = new ResponseConstructor<ResponseBase>() {
        public ResponseBase construct(HttpResponse response) {
            return new CMResponse(response);
        }
    };

    protected ResponseBase(HttpResponse response)  {
        this(response, true);
    }
    protected ResponseBase(HttpResponse response, boolean readMessageBody)  {
        if(response != null &&
                response.getStatusLine() != null) {
            statusCode = response.getStatusLine().getStatusCode();
        } else {
            statusCode = NO_RESPONSE_CODE;
        }
        if(readMessageBody) {
            InputStream jsonStream = null;
            StringWriter writer = new StringWriter();
            try {
                jsonStream = response.getEntity().getContent();
                IOUtils.copy(jsonStream, writer, JsonUtilities.ENCODING);
            } catch (IOException e) {
                LOG.error("Exception thrown", e);
            }
            messageBody = writer.toString();

            baseMap = extractResponseMap(response, messageBody);
        } else {
            messageBody = "";
            baseMap = new HashMap<String, Object>();
        }
    }

    /**
     * Internal use only
     * @param messageBody
     * @param statusCode
     */
    public ResponseBase(String messageBody, int statusCode) {
        Map<String, Object> tempNode;
        try {
            tempNode = JsonUtilities.jsonToMap(messageBody);
        } catch (ConversionException e) {
            LOG.error("Exception parsing message body: " + messageBody, e);
            tempNode = new HashMap<String, Object>();
        }
        baseMap = tempNode;
        this.statusCode = statusCode;
        this.messageBody = messageBody;
    }

    public String getMessageBody() {
        return messageBody;
    }

    protected Map<String, Object> extractResponseMap(HttpResponse response, String json) {
        Map<String, Object> responseMap = null;
        boolean noJson = (response == null || response.getEntity() == null || response.getEntity().getContentType() == null || response.getEntity().getContentType().getValue() == null ||
                !response.getEntity().getContentType().getValue().contains("json"));
        if(response == null ||
                statusCode > 202 ||
                noJson) {
            LOG.info("Received null, error, or none json response");
        }
            try {
                responseMap = JsonUtilities.jsonToMap(json);
            } catch (ConversionException e) {
                LOG.error("Failed converting response content to json", e);
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
     * Get the results from a server function call, if the request was made with a {@link com.cloudmine.api.rest.options.CMServerFunction}
     * provided in the {@link com.cloudmine.api.rest.options.CMRequestOptions}
     * @return the result object, if it exists, otherwise null
     */
    public Object getResults() {
        return getObject("result");
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
    public abstract CODE getResponseCode();

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
            return transportableRepresentation();
        } catch (ConversionException e) {
            return "Unable to convert json: " + e.getMessage();
        }
    }

    @Override
    public String transportableRepresentation() throws ConversionException {
        return JsonUtilities.mapToJson(baseMap);
    }
}

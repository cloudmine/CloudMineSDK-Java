package com.cloudmine.api.rest.response;

import com.cloudmine.api.rest.Json;
import com.cloudmine.api.rest.response.code.CMResponseCode;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

/**
 * The base response returned by requests to the cloudmine web service. Consists of the JSON response,
 * if any, and the status code.
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class CMResponse extends ResponseBase<CMResponseCode> implements Json {

    private static final Logger LOG = LoggerFactory.getLogger(CMResponse.class);
    public static final ResponseConstructor<CMResponse> CONSTRUCTOR = new ResponseConstructor<CMResponse>() {
        public CMResponse construct(HttpResponse response) {
            return new CMResponse(response);
        }

        public Future<CMResponse> constructFuture(Future<HttpResponse> response) {
            return createFutureResponse(response);
        }
    };



    public static Future<CMResponse> createFutureResponse(Future<HttpResponse> response) {
        return createFutureResponse(response, CONSTRUCTOR);
    }


    /**
     * Construct a CMResponse from an {@link HttpResponse}. It is unlikely you should be calling this
     * directly
     * @param response a response to a request to the cloudmine RESTful service
     */
    public CMResponse(HttpResponse response)  {
        super(response);
    }

    /**
     * Used by tests only
     * @param messageBody
     * @param statusCode
     */
    protected CMResponse(String messageBody, int statusCode) {
        super(messageBody, statusCode);
    }

    @Override
    public CMResponseCode getResponseCode() {
        return CMResponseCode.codeForStatus(getStatusCode());
    }
}

package com.cloudmine.api.rest.response;

import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.Transportable;
import com.cloudmine.api.rest.response.code.CMResponseCode;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base response returned by requests to the cloudmine web service. Consists of the transportable string response,
 * if any, and the status code.
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class CMResponse extends ResponseBase<CMResponseCode> implements Transportable {

    private static final Logger LOG = LoggerFactory.getLogger(CMResponse.class);
    public static final ResponseConstructor<CMResponse> CONSTRUCTOR = new ResponseConstructor<CMResponse>() {
        public CMResponse construct(HttpResponse response) {
            return new CMResponse(response);
        }

        @Override
        public CMResponse construct(String messageBody, int responseCode) throws CreationException {
            return new CMResponse(messageBody, responseCode);
        }
    };

    /**
     * Construct a CMResponse from an {@link HttpResponse}. It is unlikely you should be calling this
     * directly
     * @param response a response to a request to the cloudmine RESTful service
     */
    public CMResponse(HttpResponse response)  {
        super(response);
    }

    /**
     * Internal use only
     * @param messageBody
     * @param statusCode
     */
    public CMResponse(String messageBody, int statusCode) {
        super(messageBody, statusCode);
    }

    @Override
    public CMResponseCode getResponseCode() {
        return CMResponseCode.codeForStatus(getStatusCode());
    }
}

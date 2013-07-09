package com.cloudmine.api.rest.response;

import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.response.code.SocialGraphCode;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds the response to a social graph query.
 * <strong>Important:</strong> Use response.getStatusCode() for the HTTP Code, not response.getResponseCode(), because the code could be ANY int value.
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class SocialGraphResponse extends ResponseBase<SocialGraphCode> {
    private static final Logger LOG = LoggerFactory.getLogger(SocialGraphResponse.class);

    public static final ResponseConstructor<SocialGraphResponse> CONSTRUCTOR = new ResponseConstructor<SocialGraphResponse>() {
        @Override
        public SocialGraphResponse construct(HttpResponse response) {
            return new SocialGraphResponse(response);
        }

        @Override
        public SocialGraphResponse construct(String messageBody, int responseCode) throws CreationException {
            return new SocialGraphResponse(messageBody, responseCode);
        }
    };


    public SocialGraphResponse(HttpResponse response) {
        super(response);
    }

    public SocialGraphResponse(String message, int responseCode) {
        super(message, responseCode);
    }

    /**
     * Do not use. Use getStatusCode() instead.
     * @return SocialGraphCode The code corresponding to a success or failure.
     */
    @Override
    public SocialGraphCode getResponseCode() {
        return SocialGraphCode.codeForStatus(getStatusCode());
    }
}

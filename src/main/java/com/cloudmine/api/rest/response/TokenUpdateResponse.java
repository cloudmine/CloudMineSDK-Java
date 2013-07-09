package com.cloudmine.api.rest.response;

import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.response.code.TokenUpdateCode;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds the response for register the token with CloudMine for push notifications.
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class TokenUpdateResponse extends ResponseBase<TokenUpdateCode> {
    private static final Logger LOG = LoggerFactory.getLogger(TokenUpdateResponse.class);

    public static final ResponseConstructor<TokenUpdateResponse> CONSTRUCTOR = new ResponseConstructor<TokenUpdateResponse>() {
        @Override
        public TokenUpdateResponse construct(HttpResponse response) {
            return new TokenUpdateResponse(response);
        }

        @Override
        public TokenUpdateResponse construct(String messageBody, int responseCode) throws CreationException {
            return new TokenUpdateResponse(messageBody, responseCode);
        }
    };

    public TokenUpdateResponse(HttpResponse response) {
        super(response, false);
    }

    public TokenUpdateResponse(String msg, int responseCode) {
        super(msg, responseCode); //TODO this may not work needs testing
    }

    @Override
    public TokenUpdateCode getResponseCode() {
        return TokenUpdateCode.codeForStatus(getStatusCode());
    }
    
}

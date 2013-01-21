package com.cloudmine.api.rest.response;

import com.cloudmine.api.rest.response.code.SocialGraphCode;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

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
    };


    public SocialGraphResponse(HttpResponse response) {
        super(response);
    }

    /**
     * Do not use. Use getStatusCode() instead.
     * @return null.
     */
    @Override
    public SocialGraphCode getResponseCode() {
        return null;
    }

}

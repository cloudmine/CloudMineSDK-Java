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
 * Created with IntelliJ IDEA.
 * User: ethan
 * Date: 1/18/13
 * Time: 10:37 AM
 * To change this template use File | Settings | File Templates.
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

    @Override
    public SocialGraphCode getResponseCode() {
        return null;
    }

}

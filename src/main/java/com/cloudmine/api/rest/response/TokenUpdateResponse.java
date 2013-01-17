package com.cloudmine.api.rest.response;

import com.cloudmine.api.rest.response.code.TokenUpdateCode;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: ethan
 * Date: 1/16/13
 * Time: 10:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class TokenUpdateResponse extends ResponseBase<TokenUpdateCode> {
    private static final Logger LOG = LoggerFactory.getLogger(TokenUpdateResponse.class);

    public static final ResponseConstructor<TokenUpdateResponse> CONSTRUCTOR = new ResponseConstructor<TokenUpdateResponse>() {
        @Override
        public TokenUpdateResponse construct(HttpResponse response) {
            return new TokenUpdateResponse(response);
        }
    };

    public TokenUpdateResponse(HttpResponse response) {
        super(response, false);
    }

    @Override
    public TokenUpdateCode getResponseCode() {
        return TokenUpdateCode.codeForStatus(getStatusCode());
    }
    
}

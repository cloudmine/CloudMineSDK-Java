package com.cloudmine.api.rest.callbacks;

import com.cloudmine.api.rest.response.TokenUpdateResponse;

/**
 * Created with IntelliJ IDEA.
 * User: ethan
 * Date: 1/17/13
 * Time: 2:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class TokenResponseCallback extends CMCallback<TokenUpdateResponse> {

    public TokenResponseCallback() {
        super(TokenUpdateResponse.CONSTRUCTOR);
    }
}

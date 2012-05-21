package com.cloudmine.api.rest;

import com.cloudmine.api.UserToken;
import org.apache.http.HttpResponse;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/21/12, 5:38 PM
 */
public class LoginResponse extends CloudMineResponse {

    public static final ResponseConstructor<LoginResponse> CONSTRUCTOR = new ResponseConstructor<LoginResponse>() {
        @Override
        public LoginResponse construct(HttpResponse response) {
            return new LoginResponse(response);
        }
    };

    private final UserToken userToken;

    public LoginResponse(HttpResponse response) {
        super(response);
        userToken = new UserToken(asJson());
    }

    public UserToken userToken() {
        return userToken;
    }

}

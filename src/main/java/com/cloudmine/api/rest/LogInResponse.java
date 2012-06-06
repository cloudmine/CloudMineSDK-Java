package com.cloudmine.api.rest;

import com.cloudmine.api.UserToken;
import com.loopj.android.http.ResponseConstructor;
import org.apache.http.HttpResponse;

import java.util.concurrent.Future;

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

        @Override
        public Future<LoginResponse> constructFuture(Future<HttpResponse> futureResponse) {
            return createFutureResponse(futureResponse, CONSTRUCTOR);
        }
    };

    private final UserToken userToken;

    public LoginResponse(HttpResponse response) {
        super(response);
        if(wasSuccess()) {
            userToken = new UserToken(asJson());
        } else {
            userToken = UserToken.FAILED;
        }
    }

    public UserToken userToken() {
        return userToken;
    }

}

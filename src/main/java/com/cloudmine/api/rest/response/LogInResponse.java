package com.cloudmine.api.rest.response;

import com.cloudmine.api.CMUserToken;
import org.apache.http.HttpResponse;

import java.util.concurrent.Future;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/21/12, 5:38 PM
 */
public class LogInResponse extends CMResponse {

    public static final ResponseConstructor<LogInResponse> CONSTRUCTOR = new ResponseConstructor<LogInResponse>() {
        @Override
        public LogInResponse construct(HttpResponse response) {
            return new LogInResponse(response);
        }

        @Override
        public Future<LogInResponse> constructFuture(Future<HttpResponse> futureResponse) {
            return createFutureResponse(futureResponse, CONSTRUCTOR);
        }
    };

    private final CMUserToken userToken;

    public LogInResponse(HttpResponse response) {
        super(response);
        if(wasSuccess()) {
            userToken = CMUserToken.CMUserToken(asJson());
        } else {
            userToken = CMUserToken.FAILED;
        }
    }

    public CMUserToken userToken() {
        return userToken;
    }

}

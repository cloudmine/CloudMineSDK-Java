package com.cloudmine.api.rest.response;

import com.cloudmine.api.CMSessionToken;
import com.cloudmine.api.exceptions.JsonConversionException;
import org.apache.http.HttpResponse;

import java.util.concurrent.Future;

/**
 * Returned by the CloudMine service in response to log in requests. Includes the sessionToken used by
 * services that operate at the user level.
 * Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class LoginResponse extends CMResponse {

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

    private final CMSessionToken sessionToken;

    /**
     * Instantiate a new LoginResponse. You should probably not be calling this yourself
     * @param response a response to a log in request
     */
    public LoginResponse(HttpResponse response) {
        super(response);
        if(wasSuccess()) {
            CMSessionToken tempToken;
            try {
                tempToken = CMSessionToken.CMSessionToken(asJson());
            } catch (JsonConversionException e) {
                tempToken = CMSessionToken.FAILED;
            }
            sessionToken = tempToken;
        } else {
            sessionToken = CMSessionToken.FAILED;
        }
    }

    /**
     * the token used to authenticate this session with the server. If the request failed, it will be equal to {@link com.cloudmine.api.CMSessionToken#FAILED}
     * @return the token used to authenticate this session with the server
     */
    public CMSessionToken getSessionToken() {
        return sessionToken;
    }

}

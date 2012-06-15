package com.cloudmine.api.rest.callbacks;


import com.cloudmine.api.rest.response.LogInResponse;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/24/12, 4:52 PM
 */
public class LoginResponseCallback extends CMWebServiceCallback<LogInResponse> {

    public LoginResponseCallback() {
        super(LogInResponse.CONSTRUCTOR);
    }
}

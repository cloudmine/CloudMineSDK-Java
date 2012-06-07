package com.cloudmine.api.rest.callbacks;

import com.cloudmine.api.rest.response.LogInResponse;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/24/12, 4:52 PM
 */
public class LoginResponseCallback extends CloudMineWebServiceCallback<LogInResponse> {
    public LoginResponseCallback() {
        super(LogInResponse.CONSTRUCTOR);
    }
}

package com.cloudmine.api.rest.callbacks;


import com.cloudmine.api.rest.response.LoginResponse;

/**
 * Callback for calls that return a {@link com.cloudmine.api.rest.response.LoginResponse}
 * Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class LoginResponseCallback extends CMCallback<LoginResponse> {
    public static final LoginResponseCallback DO_NOTHING = new LoginResponseCallback();
    public LoginResponseCallback() {
        super(LoginResponse.CONSTRUCTOR);
    }
}

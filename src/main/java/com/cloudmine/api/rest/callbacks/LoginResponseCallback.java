package com.cloudmine.api.rest.callbacks;


import com.cloudmine.api.rest.response.LoginResponse;

/**
 * Callback for calls that return a {@link com.cloudmine.api.rest.response.LoginResponse}
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class LoginResponseCallback extends CMCallback<LoginResponse> {
    
    public LoginResponseCallback() {
        super(LoginResponse.CONSTRUCTOR);
    }
}

package com.cloudmine.api.rest.callbacks;

import com.cloudmine.api.rest.response.CMSocialLoginResponse;

/**
 * Callback for logging in
 */
public class CMSocialLoginResponseCallback extends CMCallback<CMSocialLoginResponse> {

    public CMSocialLoginResponseCallback() {
        super(CMSocialLoginResponse.CONSTRUCTOR);
    }
}

package com.cloudmine.api.rest.callbacks;


import com.cloudmine.api.rest.response.CMResponse;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/24/12, 4:53 PM
 */
public class CMResponseCallback extends CMWebServiceCallback<CMResponse> {
    public CMResponseCallback() {
        super(CMResponse.CONSTRUCTOR);
    }
}

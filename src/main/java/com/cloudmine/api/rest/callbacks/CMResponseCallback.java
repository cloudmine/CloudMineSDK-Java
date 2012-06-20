package com.cloudmine.api.rest.callbacks;


import com.cloudmine.api.rest.response.CMResponse;

/**
 * Base callback that receives a {@link CMResponse} in the onCompletion method
 * Copyright CloudMine LLC
 */
public class CMResponseCallback extends CMCallback<CMResponse> {
    public CMResponseCallback() {
        super(CMResponse.CONSTRUCTOR);
    }
}

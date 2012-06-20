package com.cloudmine.api.rest.callbacks;


import com.cloudmine.api.rest.response.SimpleCMObjectResponse;

/**
 * Callback for server calls that return a {@link SimpleCMObjectResponse}
 * Copyright CloudMine LLC
 */
public class SimpleCMObjectResponseCallback extends CMCallback<SimpleCMObjectResponse> {
    public SimpleCMObjectResponseCallback() {
        super(SimpleCMObjectResponse.CONSTRUCTOR);
    }
}

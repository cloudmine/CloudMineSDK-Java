package com.cloudmine.api.rest.callbacks;


import com.cloudmine.api.rest.response.SimpleCMObjectResponse;

/**
 * Callback for server calls that return a {@link SimpleCMObjectResponse}
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class SimpleCMObjectResponseCallback extends CMCallback<SimpleCMObjectResponse> {
    public SimpleCMObjectResponseCallback() {
        super(SimpleCMObjectResponse.CONSTRUCTOR);
    }
}

package com.cloudmine.api.rest.callbacks;


import com.cloudmine.api.rest.response.CMObjectResponse;

/**
 * Callback for server calls that return a {@link com.cloudmine.api.rest.response.CMObjectResponse}
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class CMObjectResponseCallback extends CMCallback<CMObjectResponse> {
    public CMObjectResponseCallback() {
        super(CMObjectResponse.CONSTRUCTOR);
    }
}

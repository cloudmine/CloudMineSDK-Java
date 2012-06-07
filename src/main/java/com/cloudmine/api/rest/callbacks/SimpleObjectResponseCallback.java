package com.cloudmine.api.rest.callbacks;

import com.cloudmine.api.rest.response.SimpleObjectResponse;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/24/12, 4:54 PM
 */
public class SimpleObjectResponseCallback extends CloudMineWebServiceCallback<SimpleObjectResponse> {
    public SimpleObjectResponseCallback() {
        super(SimpleObjectResponse.CONSTRUCTOR);
    }
}

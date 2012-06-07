package com.cloudmine.api.rest.callbacks;

import com.cloudmine.api.rest.response.CloudMineResponse;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/24/12, 4:53 PM
 */
public class CloudMineResponseCallback extends CloudMineWebServiceCallback<CloudMineResponse> {
    public CloudMineResponseCallback() {
        super(CloudMineResponse.CONSTRUCTOR);
    }
}

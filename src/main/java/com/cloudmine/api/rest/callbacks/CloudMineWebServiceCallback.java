package com.cloudmine.api.rest.callbacks;

import com.cloudmine.api.rest.CloudMineResponse;
import org.apache.http.HttpResponse;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/22/12, 6:12 PM
 */
public abstract class CloudMineWebServiceCallback<T extends CloudMineResponse> implements WebServiceCallback {

    /**
     * Only one the onCompleted methods should be overridden
     * @param response
     */
    public void onCompleted(HttpResponse response) {
        onCompleted(new CloudMineResponse(response));
    }

    /**
     * Only one the onCompleted methods should be overridden
     * @param response
     */
    public void onCompleted(CloudMineResponse response) {

    }
}

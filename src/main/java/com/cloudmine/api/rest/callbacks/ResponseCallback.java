package com.cloudmine.api.rest.callbacks;

import com.cloudmine.api.rest.CloudMineResponse;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/16/12, 3:36 PM
 */
public interface ResponseCallback extends FutureCallback<HttpResponse> {

    public void onSuccess(CloudMineResponse response);

}

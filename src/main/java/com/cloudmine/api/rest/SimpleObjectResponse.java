package com.cloudmine.api.rest;

import com.cloudmine.api.SimpleCMObject;
import com.loopj.android.http.ResponseConstructor;
import org.apache.http.HttpResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/24/12, 2:03 PM
 */
public class SimpleObjectResponse extends CloudMineResponse {
    public static final ResponseConstructor<SimpleObjectResponse> CONSTRUCTOR =
            new ResponseConstructor<SimpleObjectResponse>() {

                @Override
                public SimpleObjectResponse construct(HttpResponse response) {
                    return new SimpleObjectResponse(response);
                }

                @Override
                public Future<SimpleObjectResponse> constructFuture(Future<HttpResponse> futureResponse) {
                    return CloudMineResponse.createFutureResponse(futureResponse, this);
                }
            };


    private final List<SimpleCMObject> objects;

    public SimpleObjectResponse(HttpResponse response) {
        super(response);
        if(hasSuccess()) {
            List<SimpleCMObject> tempList = new ArrayList<SimpleCMObject>();
            objects = Collections.unmodifiableList(getSuccessObjects());
        } else {
            objects = Collections.emptyList();
        }
    }

    public List<SimpleCMObject> objects() {
        return objects;
    }
}

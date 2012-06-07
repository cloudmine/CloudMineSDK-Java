package com.cloudmine.api.rest.response;

import com.cloudmine.api.SimpleCMObject;
import com.loopj.android.http.ResponseConstructor;
import org.apache.http.HttpResponse;

import java.util.*;
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
                    return createFutureResponse(futureResponse, this);
                }
            };

    private final Map<String, SimpleCMObject> objectMap;

    public SimpleObjectResponse(HttpResponse response) {
        super(response);
        if(hasSuccess()) {
            Map<String, SimpleCMObject> tempMap = new HashMap<String, SimpleCMObject>();
            for(SimpleCMObject object : getSuccessObjects()) {
                tempMap.put(object.key(), object);
            }
            objectMap = Collections.unmodifiableMap(tempMap);
        } else {
            objectMap = Collections.emptyMap();
        }
    }

    public List<SimpleCMObject> objects() {
        return getSuccessObjects();
    }

    /**
     * Returns the object with the given key, or null if it doesn't exist
     * @param key
     * @return
     */
    public SimpleCMObject object(String key) {
        return objectMap.get(key);
    }
}

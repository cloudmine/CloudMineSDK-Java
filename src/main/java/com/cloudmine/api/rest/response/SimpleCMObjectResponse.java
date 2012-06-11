package com.cloudmine.api.rest.response;

import com.cloudmine.api.SimpleCMObject;
import org.apache.http.HttpResponse;

import java.util.*;
import java.util.concurrent.Future;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/24/12, 2:03 PM
 */
public class SimpleCMObjectResponse extends SuccessErrorResponse {
    public static final ResponseConstructor<SimpleCMObjectResponse> CONSTRUCTOR =
            new ResponseConstructor<SimpleCMObjectResponse>() {

                @Override
                public SimpleCMObjectResponse construct(HttpResponse response) {
                    return new SimpleCMObjectResponse(response);
                }

                @Override
                public Future<SimpleCMObjectResponse> constructFuture(Future<HttpResponse> futureResponse) {
                    return createFutureResponse(futureResponse, this);
                }
            };

    private final Map<String, SimpleCMObject> objectMap;

    public SimpleCMObjectResponse(HttpResponse response) {
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

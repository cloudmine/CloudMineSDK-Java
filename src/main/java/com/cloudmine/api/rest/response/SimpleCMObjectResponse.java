package com.cloudmine.api.rest.response;

import com.cloudmine.api.SimpleCMObject;
import org.apache.http.HttpResponse;

import java.util.*;
import java.util.concurrent.Future;

/**
 *  Returned by the CloudMine service in response to object fetch requests. Provides access to the
 *  {@link SimpleCMObject}s returned by the request
 * Copyright CloudMine LLC
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

    /**
     * Instantiate a new SimpleCMObjectResponse. You probably should not be calling this yourself.
     * @param response a response to an object fetch request
     */
    public SimpleCMObjectResponse(HttpResponse response) {
        super(response);
        if(hasSuccess()) {
            Map<String, SimpleCMObject> tempMap = new HashMap<String, SimpleCMObject>();
            for(SimpleCMObject object : getSuccessObjects()) {
                tempMap.put(object.getObjectId(), object);
            }
            objectMap = Collections.unmodifiableMap(tempMap);
        } else {
            objectMap = Collections.emptyMap();
        }
    }

    /**
     * Returns a List of all the SimpleCMObjects fetched by the request
     * @return
     */
    public List<SimpleCMObject> getObjects() {
        return getSuccessObjects();
    }

    /**
     * Returns the object with the given objectId, or null if it doesn't exist
     * @param objectId the objectId for the object
     * @return the object, or null if it was not retrieved
     */
    public SimpleCMObject getSimpleCMObject(String objectId) {
        return objectMap.get(objectId);
    }
}

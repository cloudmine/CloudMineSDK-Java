package com.cloudmine.api.rest.response;

import com.cloudmine.api.SimpleCMObject;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 *  Returned by the CloudMine service in response to object fetch requests. Provides access to the
 *  {@link SimpleCMObject}s returned by the request
 * Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class SimpleCMObjectResponse extends SuccessErrorResponse {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleCMObjectResponse.class);
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
    public static final String COUNT_KEY = "count";
    public static final int NO_COUNT = -1;

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

    /**
     * If this load was made with count=true (specified by using {@link com.cloudmine.api.CMPagingOptions})
     * then this will return the number of entries for the query that was made, regardless of how many results
     * were returned.
     * @return the number of entries for the query that was made, or {@link #NO_COUNT} if count=true wasn't requested, or if unable to parse the count property as an Integer
     */
    public int getCount() {
        Object countObject = getObject(COUNT_KEY);
        if(countObject != null && countObject instanceof Integer) {
            return ((Integer)countObject).intValue();
        }
        return NO_COUNT;
    }
}

package com.cloudmine.api.rest.response;

import org.apache.http.HttpResponse;

import java.util.concurrent.Future;

/**
 * Response returned whenever objects are added, updated, or deleted on the CloudMine platform.
 * Lets you check whether a specific object was updated/created/deleted/modified
 * Copyright CloudMine LLC
 */
public class ObjectModificationResponse extends SuccessErrorResponse{

    public static final ResponseConstructor<ObjectModificationResponse> CONSTRUCTOR = new ResponseConstructor<ObjectModificationResponse>() {
        @Override
        public ObjectModificationResponse construct(HttpResponse response) {
            return new ObjectModificationResponse(response);
        }

        @Override
        public Future<ObjectModificationResponse> constructFuture(Future<HttpResponse> futureResponse) {
            return createFutureResponse(futureResponse, this);
        }
    };

    /**
     * Instantiate a new ObjectModificationResponse. You probably should not be calling this yourself
     * @param response a response to an add/update/delete request
     */
    public ObjectModificationResponse(HttpResponse response) {
        super(response);
    }

    protected ObjectModificationResponse(String messageBody, int statusCode) {
        super(messageBody, statusCode);
    }

    /**
     * Check whether a specific object already existed and had its values updated
     * @param key the object key
     * @return true if the object already existed and was updated
     */
    public boolean wasUpdated(String key) {
        return ResponseValue.UPDATED.equals(getKeyResponse(key));
    }

    /**
     * Check whether a specific object was created
     * @param key the object key
     * @return true if the object associated with the given key was inserted
     */
    public boolean wasCreated(String key) {
        return ResponseValue.CREATED.equals(getKeyResponse(key));
    }

    /**
     * Check whether a specific object was deleted
     * @param key the object key
     * @return true if the object associated with the given key was deleted
     */
    public boolean wasDeleted(String key) {
        return ResponseValue.DELETED.equals(getKeyResponse(key));
    }

    /**
     * Check whether a specific object was created, deleted, or updated
     * @param key the object key
     * @return true if the object associated with the given key was created, updated, or deleted
     */
    public boolean wasModified(String key) {
        return !ResponseValue.MISSING.equals(getKeyResponse(key));
    }

    /**
     * Get the ResponseValue for the object associated with the given key
     * @param key the object key
     * @return the ResponseValue for the object associated with the given key. If the object does not exist, ResponseValue.MISSING is returned.
     */
    public ResponseValue getKeyResponse(String key) {
        Object keyedValue = getSuccessMap().get(key);
        if(keyedValue == null)
            return ResponseValue.MISSING;
        return ResponseValue.getValue(keyedValue.toString());
    }

}

package com.cloudmine.api.rest.response;

import org.apache.http.HttpResponse;

import java.util.ArrayList;
import java.util.List;
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
     * @param objectId the object objectId
     * @return true if the object already existed and was updated
     */
    public boolean wasUpdated(String objectId) {
        return ResponseValue.UPDATED.equals(getKeyResponse(objectId));
    }

    /**
     * Check whether a specific object was created
     * @param objectId the object objectId
     * @return true if the object associated with the given objectId was inserted
     */
    public boolean wasCreated(String objectId) {
        return ResponseValue.CREATED.equals(getKeyResponse(objectId));
    }

    /**
     * Check whether a specific object was deleted
     * @param objectId the object objectId
     * @return true if the object associated with the given objectId was deleted
     */
    public boolean wasDeleted(String objectId) {
        return ResponseValue.DELETED.equals(getKeyResponse(objectId));
    }

    /**
     * Check whether a specific object was created, deleted, or updated
     * @param objectId the object objectId
     * @return true if the object associated with the given objectId was created, updated, or deleted
     */
    public boolean wasModified(String objectId) {
        return !ResponseValue.MISSING.equals(getKeyResponse(objectId));
    }

    /**
     * Gets the object ids of all the objects that were deleted
     * @return the object ids of all the objects that were deleted
     */
    public List<String> getDeletedObjectIds() {
        List<String> deletedObjectIds = new ArrayList<String>();
        for(String objectId : getSuccessMap().keySet()) {
            if(wasDeleted(objectId)) {
                deletedObjectIds.add(objectId);
            }
        }
        return deletedObjectIds;
    }

    /**
     * Get the ResponseValue for the object associated with the given objectId
     * @param objectId the object objectId
     * @return the ResponseValue for the object associated with the given objectId. If the object does not exist, ResponseValue.MISSING is returned.
     */
    public ResponseValue getKeyResponse(String objectId) {
        Object keyedValue = getSuccessMap().get(objectId);
        if(keyedValue == null)
            return ResponseValue.MISSING;
        return ResponseValue.getValue(keyedValue.toString());
    }

}

package com.cloudmine.api.rest.response;

import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.response.code.ObjectModificationCode;
import org.apache.http.HttpResponse;

import java.util.*;

/**
 * Response returned whenever objects are added, updated, or deleted on the CloudMine platform.
 * Lets you check whether a specific object was updated/created/deleted/modified
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class ObjectModificationResponse extends SuccessErrorResponse<ObjectModificationCode>{

    public static final ResponseConstructor<ObjectModificationResponse> CONSTRUCTOR = new ResponseConstructor<ObjectModificationResponse>() {
        @Override
        public ObjectModificationResponse construct(HttpResponse response) {
            return new ObjectModificationResponse(response);
        }

        @Override
        public ObjectModificationResponse construct(String messageBody, int responseCode) throws CreationException {
            return new ObjectModificationResponse(messageBody, responseCode);
        }
    };

    /**
     * Instantiate a new ObjectModificationResponse. You probably should not be calling this yourself
     * @param response a response to an add/update/delete request
     */
    public ObjectModificationResponse(HttpResponse response) {
        super(response);
    }

    /**
     * Internal use only
     * @param messageBody
     * @param statusCode
     */
    public ObjectModificationResponse(String messageBody, int statusCode) {
        super(messageBody, statusCode);
    }

    @Override
    public ObjectModificationCode getResponseCode() {
        return ObjectModificationCode.codeForStatus(getStatusCode());
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
        for(String objectId : getObjectIds()) {
            if(wasDeleted(objectId)) {
                deletedObjectIds.add(objectId);
            }
        }
        return deletedObjectIds;
    }

    /**
     * Gets the object ids of all the objects that were updated
     * @return
     */
    public List<String> getUpdatedObjectIds() {
        List<String> updatedObjectIds = new ArrayList<String>();
        for(String objectId : getObjectIds()) {
            if(wasUpdated(objectId)) {
                updatedObjectIds.add(objectId);
            }
        }
        return updatedObjectIds;
    }

    /**
     * Gets the object ids of all the objects that were created
     * @return
     */
    public List<String> getCreatedObjectIds() {
        List<String> createdObjectIds = new ArrayList<String>();
        for(String objectId : getObjectIds()) {
            if(wasCreated(objectId)) {
                createdObjectIds.add(objectId);
            }
        }
        return createdObjectIds;
    }

    /**
     * Get a Map from the ObjectIds that have been modified, to how they were modified
     * @return
     */
    public Map<String, ResponseValue> getModifiedMap() {
        Map<String, ResponseValue> statusMap = new HashMap<String, ResponseValue>();
        for(String objectId : getObjectIds()) {
            statusMap.put(objectId, getKeyResponse(objectId));
        }
        return statusMap;
    }

    private Set<String> getObjectIds() {
        return getSuccessMap().keySet();
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

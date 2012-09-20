package com.cloudmine.api;

import com.cloudmine.api.exceptions.AccessException;
import com.cloudmine.api.exceptions.ConversionException;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.CMStore;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.Savable;
import com.cloudmine.api.rest.Transportable;
import com.cloudmine.api.rest.callbacks.CMCallback;
import com.cloudmine.api.rest.callbacks.Callback;
import com.cloudmine.api.rest.callbacks.CreationResponseCallback;
import com.cloudmine.api.rest.response.CreationResponse;
import com.cloudmine.api.rest.response.ObjectModificationResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Can be subclassed to allow for persisting POJOs to CloudMine. If you would like to specify a custom class name
 * (for example, for interoperability with existing iOS CMObjects), you may override getClassName(). If you do this,
 * you must also call {@link com.cloudmine.api.persistance.ClassNameRegistry#register(String, Class)} before any loads
 * or persistance occurs.
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMObject implements Transportable, Savable<ObjectModificationResponse, ObjectModificationResponse> {
    //******SEE CMApiCredentials for static declaration of Class mapping*******
    private static final Logger LOG = LoggerFactory.getLogger(CMObject.class);
    public static final String MISSING_OBJECT_ID = "";
    public static final String ACCESS_KEY = "__access__";

    private String objectId;
    private Immutable<StoreIdentifier> storeId = new Immutable<StoreIdentifier>();
    private Set<String> accessListIds = new HashSet<String>();

    /**
     * Converts the given TransportableRepresentation to an object of the given class
     * @param transportableRepresentation
     * @param objectClass
     * @param <T>
     * @return
     * @throws ConversionException
     */
    public static <T extends CMObject> T convertTransportableRepresentationToObject(String transportableRepresentation, Class<T> objectClass) throws ConversionException{
        return JsonUtilities.jsonToClass(transportableRepresentation, objectClass);
    }

    /**
     * Like {@link #convertTransportableCollectionToObjectMap} but untyped; should be used when you don't know the type,
     * or when you have a collection of multiple types.
     * @param transportableRepresentations
     * @return
     */
    public static Map<String, CMObject> convertTransportableCollectionToObjectMap(String transportableRepresentations) {
        return JsonUtilities.jsonToClassMap(transportableRepresentations);
    }

    /**
     * Convert a transportable representation of a collection of objects keyed by their id's, to a Map of those keys to the objects
     * @param transportableCollection a transportable representation containing only objects of type T
     * @param objectClass the class of objects contained within the Transportable representation
     * @param <T> the type of the objects contained in transportableCollection
     * @return a Map from object ids to objects of type T
     * @throws ConversionException  if unable to convert, either because the given representation is of a different class, or because it is malformed.
     */
    public static <T extends CMObject> Map<String, T> convertTransportableCollectionToObjectMap(String transportableCollection, Class<T> objectClass) throws ConversionException {
        return JsonUtilities.jsonToClassMap(transportableCollection, objectClass);
    }

    protected static String generateUniqueObjectId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Create a new CMObject with a randomly generated object id
     */
    public CMObject() {
        this(generateUniqueObjectId());
    }

    /**
     * Create a new CMObject with a specific object id
     * @param objectId a non null object id for this CMObject
     * @throws NullPointerException if given a null objectid
     */
    public CMObject(String objectId) throws NullPointerException {
        this(objectId,  true);
    }

    protected CMObject(String objectId, boolean hasObjectid) {
        if(objectId == null && hasObjectid)
            throw new NullPointerException("Cannot have a null objectId");
        if(hasObjectid) {
            this.objectId = objectId;
        }
    }

    /**
     * Create a new CMObject that does not have an object id. This is useful for subobjects
     * @param autogenerateObjectId if true, equivalent to {@link #CMObject()}, otherwise have no object id
     */
    public CMObject(boolean autogenerateObjectId) {
        if(autogenerateObjectId)
            this.objectId = generateUniqueObjectId();
    }

    @Override
    public String transportableRepresentation() throws ConversionException {
        return JsonUtilities.cmobjectsToJson(this);
    }


    /**
     * Get a representation of this object in the form "objectId":{contents}
     * @return a representation of this object in the form "objectId":{contents}
     * @throws ConversionException if this object cannot be converted to transportable
     */
    public String asKeyedObject() throws ConversionException {
        return JsonUtilities.unwrap(transportableRepresentation());
    }

    @JsonIgnore
    public boolean setSaveWith(StoreIdentifier identifier) {
        LOG.debug("StoreId is current: " + storeId + " and if unset will be set to " + identifier);
        return storeId.setValue(identifier);
    }

    /**
     * Set that this object should be saved at the User level, and should be saved using the given CMSessionToken.
     * @param user the user to save this CMObject with
     * @return true if the value was set; false if it has already been set OR null was passed in
     */
    @JsonIgnore
    public boolean setSaveWith(CMUser user) {
        try {
            return setSaveWith(StoreIdentifier.StoreIdentifier(user));
        } catch(CreationException e) {
            LOG.error("CreationException thrown, setSaveWith not set", e);
            return false;
        }
    }

    /**
     * Gets the StoreIdentifier which defines where this CMObject will be saved. If it has not yet been set, {@link StoreIdentifier#DEFAULT} is returned
     * @return the StoreIdentifier which defines where this CMObject will be saved. If it has not yet been set, {@link StoreIdentifier#DEFAULT} is returned
     */
    @JsonIgnore
    public StoreIdentifier getSavedWith() {
        return storeId.value(StoreIdentifier.DEFAULT);
    }

    /**
     * Check whether this CMObject saves to a particular level
     * @param level the level to check
     * @return true if this saves with the given level
     */
    public boolean isOnLevel(ObjectLevel level) {
        return getSavedWith().isLevel(level);
    }

    @Override
    @JsonIgnore
    public boolean isUserLevel() {
        return isOnLevel(ObjectLevel.USER);
    }

    @Override
    @JsonIgnore
    public boolean isApplicationLevel() {
        return isOnLevel(ObjectLevel.APPLICATION);
    }

    /**
     * Allow the user's associated with the given list access to this object. The access they get depends on the
     * permissions defined by the list. The given list must have an object id
     * @param list
     */
    public void grantAccess(CMAccessList list) {
        if(list == null)
            return;
        accessListIds.add(list.getObjectId());
    }

    public void setAccessListIds(Set<String> accessListIds) {
        this.accessListIds = accessListIds;
    }

    @JsonProperty(ACCESS_KEY)
    @JsonSerialize(include= JsonSerialize.Inclusion.NON_EMPTY)
    public Set<String> getAccessListIds() {
        return accessListIds;
    }

    /**
     * Save this object in its associated store; if you have not specified this with {@link #setSaveWith(StoreIdentifier)}
     * then it saves to the APPLICATION store. Once a CMObject has been saved, it cannot be saved to a
     * different
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing this and doing something you shouldn't be with overriding transportableRepresentation
     * @throws CreationException if CMApiCredentials has not been initialized properly
     */
    public void save() throws ConversionException, CreationException {
        save(CMCallback.<ObjectModificationResponse>doNothing());
    }
    /**
     * Save this object in its associated store; if you have not specified this with {@link #setSaveWith(StoreIdentifier)}
     * then it saves to the APPLICATION store. Once a CMObject has been saved, it cannot be saved to a
     * different
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing this and doing something you shouldn't be with overriding transportableRepresentation
     * @throws CreationException if CMApiCredentials has not been initialized properly
     */
    public void save(Callback<ObjectModificationResponse> callback) throws CreationException, ConversionException {
        store().saveObject(this, callback);
    }

    public void saveWithUser(CMUser user) throws CreationException, ConversionException {
        saveWithUser(user, CMCallback.<ObjectModificationResponse>doNothing());
    }
    /**
     * Save this object in in the given CMUser's store. If {@link #setSaveWith(StoreIdentifier)} has already been called
     * Once a CMObject has been saved, it cannot be saved to a
     * different
     * @param user the user to save this object with
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing this and doing something you shouldn't be with overriding transportableRepresentation
     * @throws CreationException if CMApiCredentials has not been initialized properly
     * @throws com.cloudmine.api.exceptions.AccessException if setSaveWith has already been set
     */
    public void saveWithUser(CMUser user, Callback<ObjectModificationResponse> callback) throws CreationException, AccessException, ConversionException{
        boolean wasAlreadySet = !setSaveWith(user);
        boolean notSameUser = wasAlreadySet && //skip the check if it wasn't already set; still check below in if statement for clarity
                !user.equals(getUser());
        if(wasAlreadySet &&
                notSameUser) {
            throw new AccessException("Cannot save with user if saveWith has already been set");
        }
        save(callback);
    }

    /**
     * See {@link #delete(com.cloudmine.api.rest.callbacks.Callback)}
     */
    public void delete() {
        delete(CMCallback.<ObjectModificationResponse>doNothing());
    }

    /**
     * Delete this object, then run the given callback
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     */
    public void delete(Callback<ObjectModificationResponse> callback) {
        store().deleteObject(this, callback);
    }

    /**
     * This method should be used to check date equality when overriding {@link #equals(Object)}, as
     * serialized dates are stored in seconds.
     * @param firstDate a null possible date
     * @param secondDate a null possible date
     * @return true if the two dates represent the same second in time
     */
    public static boolean dateEquals(Date firstDate, Date secondDate) {
        if((firstDate == null && secondDate != null) ||
                (firstDate != null && secondDate == null)) {
            return false;
        }
        if(firstDate == null && secondDate == null) {
            return true;
        }

        int firstSeconds = firstDate.getSeconds();
        int secondSeconds = secondDate.getSeconds();
        return firstSeconds == secondSeconds;
    }

    @Override
    @JsonIgnore
    public CMUser getUser() {
        return getSavedWith().getUser();
    }

    @Override
    @JsonProperty(JsonUtilities.OBJECT_ID_KEY)
    /**
     * Get the objectId for this CMObject. In certain cases this may not be set; for example, CMUsers do not have an
     * object id until they have been persisted. In this case, {@link #MISSING_OBJECT_ID} is returned. You can also
     * check for the existence of an objectId by calling {@link #hasObjectId()}
     * @return The unique objectId for this object, or {@link #MISSING_OBJECT_ID} if {@link #hasObjectId()} returns false
     */
    public String getObjectId() {
        return objectId == null ? MISSING_OBJECT_ID : objectId;
    }

    /**
     * Think real hard before using this method. If this object has already been saved, changing the objectId will cause
     * a new copy to be saved. If the objectId is being used as a key in a Map, then changing it will not update the maps.
     * If you change the objectId of an object being managed by the store, if you query the store by objectId it will still
     * be under its old value.
     * Basically, STAY AWAY unless you have a very good reason, or are only calling before the object is used by anything.
     * @param objectId
     */
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    @JsonIgnore
    public boolean hasObjectId() {
        return objectId != null;
    }

    @JsonProperty("__class__")
    public String getClassName() {
        return getClass().getName();
    }

    /**
     * This wraps the given callback in a {@link com.cloudmine.api.rest.callbacks.CreationResponseCallback} that will set this CMUser's object id on
     * success, and then call {@link Callback#onCompletion(Object)} passing in the {@link com.cloudmine.api.rest.response.CreationResponse}
     * You probably don't need to be calling this ever
     * @param callback
     * @return
     */
    public final CreationResponseCallback setObjectIdOnCreation(final Callback<CreationResponse> callback) {
        return new CreationResponseCallback() {
            public void onCompletion(CreationResponse response) {
                try {
                    if(response.wasSuccess()) {
                        setObjectId(response.getObjectId());
                    }
                } finally {
                    callback.onCompletion(response);
                }
            }
        };
    }

    protected CMStore store() throws CreationException {
        return CMStore.getStore(storeId.value(StoreIdentifier.DEFAULT));
    }

    public String toString() {
        try {
            return transportableRepresentation();
        } catch(Exception e) {
            return super.toString();
        }
    }
}

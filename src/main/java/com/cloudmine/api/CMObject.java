package com.cloudmine.api;

import com.cloudmine.api.exceptions.AccessException;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.exceptions.JsonConversionException;
import com.cloudmine.api.rest.CMStore;
import com.cloudmine.api.rest.Json;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.Savable;
import com.cloudmine.api.rest.callbacks.Callback;
import com.cloudmine.api.rest.callbacks.CreationResponseCallback;
import com.cloudmine.api.rest.response.CreationResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Can be subclassed to allow for persisting POJOs to CloudMine. If you would like to specify a custom class name
 * (for example, for interoperability with existing iOS CMObjects), you may override getClassName(). If you do this,
 * you must also call {@link com.cloudmine.api.persistance.ClassNameRegistry#register(String, Class)} before any loads
 * or persistance occurs.
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMObject implements Json, Savable {
    private static final Logger LOG = LoggerFactory.getLogger(CMObject.class);
    public static final String MISSING_OBJECT_ID = "";


    private String objectId;
    private Immutable<StoreIdentifier> storeId = new Immutable<StoreIdentifier>();
    private Set<String> accessListIds = new HashSet<String>();

    protected static String generateUniqueObjectId() {
        return UUID.randomUUID().toString();
    }

    protected CMObject() {
        this(generateUniqueObjectId());
    }

    protected CMObject(String objectId) {
        this.objectId= objectId;
    }

    protected CMObject(boolean autogenerateObjectId) {
        if(autogenerateObjectId)
            this.objectId = generateUniqueObjectId();
    }

    @Override
    public String asJson() throws JsonConversionException {
        return JsonUtilities.objectsToJson(this);
    }


    /**
     * Get a representation of this object in the form "objectId":{contents}
     * @return a representation of this object in the form "objectId":{contents}
     * @throws JsonConversionException if this object cannot be converted to JSON
     */
    public String asKeyedObject() throws JsonConversionException {
        return null; //TODO this shouldn't return null I'm thinking
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
     * Save this object in its associated store; if you have not specified this with {@link #setSaveWith(StoreIdentifier)}
     * then it saves to the APPLICATION store. Once a CMObject has been saved, it cannot be saved to a
     * different
     * @throws JsonConversionException if unable to convert to JSON; this should not happen unless you are subclassing this and doing something you shouldn't be
     * @throws CreationException if CMApiCredentials has not been initialized properly
     */
    public void save() throws JsonConversionException, CreationException {
        save(Callback.DO_NOTHING);
    }
    /**
     * Save this object in its associated store; if you have not specified this with {@link #setSaveWith(StoreIdentifier)}
     * then it saves to the APPLICATION store. Once a CMObject has been saved, it cannot be saved to a
     * different
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @throws JsonConversionException if unable to convert to JSON; this should not happen unless you are subclassing this and doing something you shouldn't be
     * @throws CreationException if CMApiCredentials has not been initialized properly
     */
    public void save(Callback callback) throws CreationException, JsonConversionException {
        store().saveObject(this, callback);
    }

    public void saveWithUser(CMUser user) throws CreationException, JsonConversionException {
        saveWithUser(user, Callback.DO_NOTHING);
    }
    /**
     * Save this object in in the given CMUser's store. If {@link #setSaveWith(StoreIdentifier)} has already been called
     * Once a CMObject has been saved, it cannot be saved to a
     * different
     * @param user the user to save this object with
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @throws JsonConversionException if unable to convert to JSON; this should not happen unless you are subclassing this and doing something you shouldn't be
     * @throws CreationException if CMApiCredentials has not been initialized properly
     * @throws com.cloudmine.api.exceptions.AccessException if setSaveWith has already been set
     */
    public void saveWithUser(CMUser user, Callback callback) throws CreationException, AccessException, JsonConversionException{
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

    void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    @JsonIgnore
    public boolean hasObjectId() {
        return objectId == null;
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
    public final CreationResponseCallback setObjectIdOnCreation(final Callback callback) {
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
}

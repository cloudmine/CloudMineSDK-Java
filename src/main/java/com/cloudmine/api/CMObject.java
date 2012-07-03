package com.cloudmine.api;

import com.cloudmine.api.exceptions.AccessException;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.exceptions.JsonConversionException;
import com.cloudmine.api.rest.CMStore;
import com.cloudmine.api.rest.Json;
import com.cloudmine.api.rest.Savable;
import com.cloudmine.api.rest.callbacks.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMObject implements Json, Savable {
    private static final Logger LOG = LoggerFactory.getLogger(CMObject.class);
    private final String objectId;
    private Immutable<StoreIdentifier> storeId = new Immutable<StoreIdentifier>();



    protected static String generateUniqueObjectId() {
        return UUID.randomUUID().toString();
    }

    protected CMObject() {
        this(generateUniqueObjectId());
    }

    protected CMObject(String objectId) {
        this.objectId = objectId;
    }

    @Override
    public String asJson() throws JsonConversionException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    /**
     * Get a representation of this object in the form "objectId":{contents}
     * @return a representation of this object in the form "objectId":{contents}
     * @throws JsonConversionException if this object cannot be converted to JSON
     */
    public String asKeyedObject() throws JsonConversionException {
        return null;
    }


    public boolean setSaveWith(StoreIdentifier identifier) {
        LOG.debug("StoreId is current: " + storeId + " and if unset will be set to " + identifier);
        return storeId.setValue(identifier);
    }

    /**
     * Set that this object should be saved at the User level, and should be saved using the given CMSessionToken.
     * @param user the user to save this CMObject with
     * @return true if the value was set; false if it has already been set OR null was passed in
     */
    public boolean setSaveWith(CMUser user) {
        try {
            return setSaveWith(new StoreIdentifier(user));
        } catch(CreationException e) {
            LOG.error("CreationException thrown, setSaveWith not set", e);
            return false;
        }
    }

    /**
     * Gets the StoreIdentifier which defines where this CMObject will be saved. If it has not yet been set, {@link StoreIdentifier#DEFAULT} is returned
     * @return the StoreIdentifier which defines where this CMObject will be saved. If it has not yet been set, {@link StoreIdentifier#DEFAULT} is returned
     */
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
    public boolean isUserLevel() {
        return isOnLevel(ObjectLevel.USER);
    }

    @Override
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

    @Override
    public CMUser getUser() {
        return getSavedWith().getUser();
    }

    @Override
    public String getObjectId() {
        return objectId;
    }

    public String getClassName() {
        return null;
    }

    private CMStore store() throws CreationException {
        return CMStore.getStore(storeId.value(StoreIdentifier.DEFAULT));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CMObject cmObject = (CMObject) o;

        if (!objectId.equals(cmObject.objectId)) return false;
        if (!storeId.equals(cmObject.storeId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = objectId.hashCode();
        result = 31 * result + storeId.hashCode();
        return result;
    }
}

package com.cloudmine.api.rest;

import com.cloudmine.api.JavaCMUser;
import com.cloudmine.api.JavaCMUser;
import com.cloudmine.api.ObjectLevel;
import com.cloudmine.api.StoreIdentifier;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.exceptions.ConversionException;
import com.cloudmine.api.rest.callbacks.Callback;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A common interface that can be shared between different objects that can be saved to cloudmine
 * Copyright CloudMine LLC
 */
public interface Savable<SAVE_CALLBACK, DELETE_CALLBACK> extends Transportable {

    /**
     * Set what store to save this object with. If this is not set, it is assumed to be saved with
     * the default, app level store. Once the StoreIdentifier is set, it cannot be changed.
     * @param identifier the identifier for the store this should be saved with. calling setSaveWith(null)
     *                   is the same as not calling setSaveWith, and false will be returned
     * @return true if the value was set; false if it has already been set OR null was passed in
     */
    @JsonIgnore
    public boolean setSaveWith(StoreIdentifier identifier);

    /**
     * Set that this object should be saved at the User level, and should be saved using the given CMUser
     * @param user the user to save this object with
     * @return true if the value was set; false if it has already been set OR null was passed in
     * @throws com.cloudmine.api.exceptions.CreationException if user is null
     */
    @JsonIgnore
    public boolean setSaveWith(JavaCMUser user);

    /**
     * Gets the StoreIdentifier which defines where this object will be saved. If it has not yet been set, {@link StoreIdentifier#DEFAULT} is returned
     * @return the StoreIdentifier which defines where this object will be saved. If it has not yet been set, {@link StoreIdentifier#DEFAULT} is returned
     */
    public StoreIdentifier getSavedWith();

    /**
     * Check whether this object saves to a particular level
     * @param level the level to check
     * @return true if this saves with the given level
     */
    public boolean isOnLevel(ObjectLevel level);

    /**
     * Check whether this object is saved to the user level
     * @return true if user level, false otherwise
     */
    public boolean isUserLevel();

    /**
     * Check whether this object is saved to the application level
     * @return true if application level, false otherwise
     */
    public boolean isApplicationLevel();

    /**
     * Asynchronously save this object to its store. If no store has been set, it saves to the app
     * level store.
     * @throws com.cloudmine.api.exceptions.ConversionException if unable to convert this object to a transportable representation; should not happen unless you have overridden transportableRepresentation
     * @throws com.cloudmine.api.exceptions.CreationException if {@link com.cloudmine.api.CMApiCredentials#initialize(String, String)} has not been called
     */
    public void save() throws ConversionException, CreationException;

    /**
     * Asynchronously save this object to its store. If no store has been set, it saves to the app
     * level store.
     * @param callback a Callback that will be called once this request completes
     * @throws ConversionException if unable to convert this object to transportable representation; should not happen unless you have overridden transportableRepresentation
     * @throws CreationException if {@link com.cloudmine.api.CMApiCredentials#initialize(String, String)} has not been called
     */
    public void save(Callback<SAVE_CALLBACK> callback) throws CreationException, ConversionException;

    /**
     * See {@link #delete(com.cloudmine.api.rest.callbacks.Callback)}
     */
    public void delete();

    /**
     * Delete this savable object, then run the given callback
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link com.cloudmine.api.rest.response.ObjectModificationResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in
     */
    public void delete(Callback<DELETE_CALLBACK> callback);

    /**
     * Return the CMUser this object belongs to, if it exists. Careful - this can return null if no user has been set
     * @return if this object is saved to the USER level, and a user has been set, its user; otherwise null.
     */
    public JavaCMUser getUser();

    /**
     * Get the objectId that uniquely identifies this object
     * @return the objectId that uniquely identifies this object
     */
    public String getObjectId();

}

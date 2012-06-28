package com.cloudmine.api.rest;

import com.cloudmine.api.*;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.exceptions.JsonConversionException;
import com.cloudmine.api.rest.callbacks.Callback;
import com.cloudmine.api.rest.callbacks.LoginResponseCallback;
import com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback;
import com.cloudmine.api.rest.response.FileLoadResponse;
import com.cloudmine.api.rest.response.LoginResponse;
import com.cloudmine.api.rest.response.ObjectModificationResponse;
import com.cloudmine.api.rest.response.SimpleCMObjectResponse;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * The main class for interacting with the CloudMine API. Stores can operate on both the user or application level
 * Preconditions for use:<br>
 * {@link DeviceIdentifier#initialize(android.content.Context)} has been called with the activity context<br>
 * {@link CMApiCredentials#initialize(String, String)} has been called with the application identifier and API key<br>
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class CMStore {

    private static final Map<StoreIdentifier, CMStore> storeMap = new HashMap<StoreIdentifier, CMStore>();

    /**
     * Get the default store. As a default there will be no CMSessionToken associated with this store, but
     * one may be set. Calls to this method will always return the same CMStore instance, so associated
     * CMUserTokens will persist
     * @return the default store
     * @throws CreationException if the preconditions for use are not satisfied
     */
    public static CMStore getStore() throws CreationException {
        return getStore(StoreIdentifier.DEFAULT);
    }

    /**
     * Get the store associated with the given StoreIdentifer. If this is the first time this method has
     * been called with the given StoreIdentifier, a new store will be instantiated. Subsequent calls will
     * always return the same store.
     * @param storeId the identifier for the store. If null, defaults to {@link StoreIdentifier#DEFAULT}
     * @return the store associated with the given StoreIdentifier
     * @throws CreationException if the preconditions for use are not satisfied
     */
    public static CMStore getStore(StoreIdentifier storeId) throws CreationException {
        if(storeId == null) {
            storeId = StoreIdentifier.DEFAULT;
        }
        CMStore store = storeMap.get(storeId);
        if(store == null) {
            store = CMStore.CMStore(storeId);
            storeMap.put(storeId, store);
        }
        return store;
    }

    /**
     * Retrieve the CMStore associated with the given CMSessionToken, or creates a new CMStore and returns it
     * if no appropriate store already exists
     * @param user A non null user. If the user is not logged in
     * @return a CMStore whose user level methods will interact with the user associated with the passed in CMSessionToken
     * @throws CreationException if user was null or if the preconditions for use are not satisfied
     */
    public static CMStore getStore(CMUser user) throws CreationException {
        return getStore(StoreIdentifier.StoreIdentifier(user));
    }

    /**
     * Instantiate a new CMStore with the given StoreIdentifier. Differs from {@link CMStore#getStore}
     * as it always returns a new instance
     * @param identifier the identifier for the store. If null, defaults to {@link StoreIdentifier#DEFAULT}
     * @return the store
     * @throws CreationException if the preconditions for use are not satisfied
     */
    public static CMStore CMStore(StoreIdentifier identifier) throws CreationException {
        return new CMStore(identifier);
    }


    /**
     * Instantiate a new CMStore with the default StoreIdentifier. Differs from {@link CMStore#getStore}
     * as it always returns a new instance
     * @return the store
     * @throws CreationException if the preconditions for use are not satisfied
     */
    public static CMStore CMStore() throws CreationException {
        return CMStore(StoreIdentifier.DEFAULT);
    }

    private final CMWebService applicationService;
    private final Immutable<CMUser> user = new Immutable<CMUser>();
    private final Map<String, SimpleCMObject> objects = new ConcurrentHashMap<String, SimpleCMObject>();

    private CMStore(StoreIdentifier identifier) throws CreationException {
        if(identifier == null) {
            identifier = StoreIdentifier.DEFAULT;
        }
        if(identifier.isUserLevel()) {
            setUser(identifier.getUser());
        }
        applicationService = CMWebService.getService();
    }

    private CMUser user() {
        return user.valueOrThrow();
    }

    private final SimpleCMObjectResponseCallback objectLoadUpdateStoreCallback(final Callback callback, final StoreIdentifier identifier) {
        return new SimpleCMObjectResponseCallback() {
            public void onCompletion(SimpleCMObjectResponse response) {
                try {
                    if(response.wasSuccess()) {
                        List<SimpleCMObject> simpleCMObjects = response.getSuccessObjects();
                        addObjects(simpleCMObjects);
                        for(SimpleCMObject object : simpleCMObjects) {
                            object.setSaveWith(identifier);
                        }
                    }
                }finally {
                    callback.onCompletion(response);
                }
            }
        };
    }

    /*****************************OBJECTS********************************/

    /**
     * Asynchronously save the object based on the StoreIdentifier associated with it. If no StoreIdentifier is
     * present, default (app level) is used; however, the object's StoreIdentifier is not updated.
     * NOTE: No matter what user is associated with the object to save, the store always saves the object with the user associated with the store.
     * @param object the object to save
     * @throws JsonConversionException if unable to convert the SimpleCMObject to json.
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void saveObject(SimpleCMObject object) throws JsonConversionException, CreationException {
        saveObject(object, Callback.DO_NOTHING);
    }

    /**
     * Asynchronously save the object based on the StoreIdentifier associated with it. If no StoreIdentifier is
     * present, default (app level) is used; however, the object's StoreIdentifier is not updated.
     * NOTE: No matter what user is associated with the object to save, the store always saves the object with the user associated with the store.
     * @param object the object to save
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @throws JsonConversionException if unable to convert the SimpleCMObject to json.
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it and this object is UserLevel
     */
    public void saveObject(SimpleCMObject object, Callback callback) throws JsonConversionException, CreationException {
        saveObject(object, callback, CMRequestOptions.NONE);
    }

    /**
     * Asynchronously save the object based on the StoreIdentifier associated with it. If no StoreIdentifier is
     * present, default (app level) is used; and the object's StoreIdentifier is updated.
     * NOTE: No matter what user is associated with the object to save, the store always saves the object with the user associated with the store.
     * @param object the object to save
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     * @throws JsonConversionException if unable to convert the SimpleCMObject to json.
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void saveObject(final SimpleCMObject object, final Callback callback, final CMRequestOptions options) throws JsonConversionException, CreationException {
        addObject(object);
        if(object.isOnLevel(ObjectLevel.USER)) {
            user().login(new LoginResponseCallback() {
               public void onCompletion(LoginResponse ignoredResponse) {
                   userService().asyncInsert(object, callback, options);
               }
            });
        } else {
            object.setSaveWith(StoreIdentifier.applicationLevel());
            applicationService.asyncInsert(object, callback, options);
        }
    }

    /**
     * Delete the given object from CloudMine. If no StoreIdentifier is present, default (app level) is
     * used; however, the object's StoreIdentifier is not updated.
     * NOTE: No matter what user is associated with the object to save, the store always deletes the object with the user associated with the store.
     * @param object to delete; this is done based on the object id, its values are ignored
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void deleteObject(SimpleCMObject object) throws CreationException {
        deleteObject(object, Callback.DO_NOTHING);
    }

    /**
     * Delete the given object from CloudMine. If no StoreIdentifier is present, default (app level) is
     * used; however, the object's StoreIdentifier is not updated.
     * NOTE: No matter what user is associated with the object to save, the store always deletes the object with the user associated with the store.
     * @param object to delete; this is done based on the object id, its values are ignored
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @return a Future containing the {@link ObjectModificationResponse} which can be queried to check the success of this operation
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void deleteObject(SimpleCMObject object, Callback callback) throws CreationException {
        deleteObject(object, callback, CMRequestOptions.NONE);
    }

    /**
     * Delete the given object from CloudMine. If no {@link StoreIdentifier} is present, default (app level) is
     * used; however, the object's StoreIdentifier is not updated.
     * NOTE: No matter what user is associated with the object to save, the store always deletes the object with the user associated with the store.
     * @param object to delete; this is done based on the object id, its values are ignored. Must not be null
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void deleteObject(final SimpleCMObject object, final Callback callback, final CMRequestOptions options) throws CreationException {
        removeObject(object);
        if(object.isOnLevel(ObjectLevel.USER)) {
            user().login(new LoginResponseCallback() {
                public void onCompletion(LoginResponse response) {
                    userService().asyncDeleteObject(object, callback, options);
                }
            });
        } else {
            applicationService.asyncDeleteObject(object, callback, options);
        }
    }

    /**
     * Retrieve all the application level objects
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> loadAllApplicationObjects() {
        return loadAllApplicationObjects(Callback.DO_NOTHING);
    }

    /**
     * Retrieve all the application level objects and pass the results into the given callback.
     * @param callback a Callback that expects a {@link SimpleCMObjectResponse}. It is recommended that a {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> loadAllApplicationObjects(Callback callback) {
        return loadAllApplicationObjects(callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the application level objects and pass the results into the given callback.
     * @param callback a Callback that expects a {@link SimpleCMObjectResponse}. It is recommended that a {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> loadAllApplicationObjects(Callback callback, CMRequestOptions options) {
        return applicationService.asyncLoadObjects(objectLoadUpdateStoreCallback(callback, StoreIdentifier.DEFAULT),
                options);
    }

    /**
     * Retrieve all the application level objects
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void loadAllUserObjects() throws CreationException {
        loadAllUserObjects(Callback.DO_NOTHING);
    }

    /**
     * Retrieve all the user level objects and pass the results into the given callback.
     * @param callback a Callback that expects a {@link SimpleCMObjectResponse}. It is recommended that a {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void loadAllUserObjects(Callback callback) throws CreationException {
        loadAllUserObjects(callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the user level objects and pass the results into the given callback.
     * @param callback a Callback that expects a {@link SimpleCMObjectResponse}. It is recommended that a {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @throws CreationException if this CMStore does not have a CMUser associated with it
     */
    public void loadAllUserObjects(final Callback callback, final CMRequestOptions options) throws CreationException {
        user().login(new LoginResponseCallback() {
           public void onCompletion(LoginResponse response) {
               userService().asyncLoadObjects(objectLoadUpdateStoreCallback(callback, StoreIdentifier.StoreIdentifier(user())), options);
           }
        });
    }

    /**
     * Retrieve all the application level objects with the given objectIds
     * @param objectIds the top level objectIds of the objects to retrieve
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> loadApplicationObjectsWithObjectIds(Collection<String> objectIds) {
        return loadApplicationObjectsWithObjectIds(objectIds, Callback.DO_NOTHING);
    }

    /**
     * Retrieve all the application level objects with the given objectIds
     * @param objectIds the top level objectIds of the objects to retrieve
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used here
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> loadApplicationObjectsWithObjectIds(Collection<String> objectIds, Callback callback) {
        return loadApplicationObjectsWithObjectIds(objectIds, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the application level objects with the given objectIds
     * @param objectIds the top level objectIds of the objects to retrieve
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used here
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> loadApplicationObjectsWithObjectIds(Collection<String> objectIds, Callback callback, CMRequestOptions options) {
        return applicationService.asyncLoadObjects(objectIds, objectLoadUpdateStoreCallback(callback, StoreIdentifier.applicationLevel()), options);
    }

    /**
     * Retrieve all the application level objects with the given objectIds
     * @param objectId the top level objectIds of the objects to retrieve
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> loadApplicationObjectWithObjectId(String objectId) {
        return loadApplicationObjectWithObjectId(objectId, Callback.DO_NOTHING);
    }
    /**
     * Retrieve all the application level objects with the given objectIds
     * @param objectId the top level objectIds of the objects to retrieve
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used here
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> loadApplicationObjectWithObjectId(String objectId, Callback callback) {
        return loadApplicationObjectWithObjectId(objectId, callback, CMRequestOptions.NONE);
    }
    /**
     * Retrieve all the application level objects with the given objectIds
     * @param objectId the top level objectIds of the objects to retrieve
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used here
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> loadApplicationObjectWithObjectId(String objectId, Callback callback, CMRequestOptions options) {
        return applicationService.asyncLoadObject(objectId, objectLoadUpdateStoreCallback(callback, StoreIdentifier.applicationLevel()), options);
    }

    /**
     * Retrieve all the user level objects with the given objectIds
     * @param objectIds the top level objectIds of the objects to retrieve
     * @throws CreationException if this CMStore does not have a CMUser associated with it
     */
    public void loadUserObjectsWithObjectIds(Collection<String> objectIds) throws CreationException {
        loadUserObjectsWithObjectIds(objectIds, Callback.DO_NOTHING);
    }

    /**
     * Retrieve all the user level objects with the given objectIds
     * @param objectIds the top level objectIds of the objects to retrieve
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used here
     * @throws CreationException if this CMStore does not have a CMUser associated with it
     */
    public void loadUserObjectsWithObjectIds(Collection<String> objectIds, Callback callback) throws CreationException {
        loadUserObjectsWithObjectIds(objectIds, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the user level objects with the given objectIds
     * @param objectIds the top level objectIds of the objects to retrieve
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used here
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @throws CreationException if this CMStore does not have a CMUser associated with it
     */
    public void loadUserObjectsWithObjectIds(final Collection<String> objectIds, final Callback callback, final CMRequestOptions options) throws CreationException {
        user().login(new LoginResponseCallback() {
           public void onCompletion(LoginResponse response) {
               userService().asyncLoadObjects(objectIds, objectLoadUpdateStoreCallback(callback, StoreIdentifier.StoreIdentifier(user())), options);
           }
        });
    }

    /**
     * Retrieve all the user level objects that match the given search
     * @param search the search string to use. For more information on syntax. See <a href="https://cloudmine.me/docs/object-storage#query_syntax">Search query syntax</a>
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void loadUserObjectsSearch(String search) throws CreationException {
        loadUserObjectsSearch(search, Callback.DO_NOTHING);
    }

    /**
     * Retrieve all the user level objects that match the given search
     * @param search the search string to use. For more information on syntax. See <a href="https://cloudmine.me/docs/object-storage#query_syntax">Search query syntax</a>
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used here
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void loadUserObjectsSearch(String search, Callback callback) throws CreationException {
        loadUserObjectsSearch(search, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the user level objects that match the given search
     * @param search the search string to use. For more information on syntax. See <a href="https://cloudmine.me/docs/object-storage#query_syntax">Search query syntax</a>
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used here
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void loadUserObjectsSearch(final String search, final Callback callback, final CMRequestOptions options) throws CreationException {
        user().login(new LoginResponseCallback() {
            @Override
            public void onCompletion(LoginResponse response) {
                userService().asyncSearch(search, objectLoadUpdateStoreCallback(callback, StoreIdentifier.StoreIdentifier(user())), options);
            }
        });
    }

    /**
     * Retrieve all the application level objects that match the given search
     * @param search the search string to use. For more information on syntax. See <a href="https://cloudmine.me/docs/object-storage#query_syntax">Search query syntax</a>
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> loadApplicationObjectsSearch(String search) {
        return loadApplicationObjectsSearch(search, Callback.DO_NOTHING);
    }

    /**
     * Retrieve all the application level objects that match the given search
     * @param search the search string to use. For more information on syntax. See <a href="https://cloudmine.me/docs/object-storage#query_syntax">Search query syntax</a>
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used here
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> loadApplicationObjectsSearch(String search, Callback callback) {
        return loadApplicationObjectsSearch(search, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the application level objects that match the given search
     * @param search the search string to use. For more information on syntax. See <a href="https://cloudmine.me/docs/object-storage#query_syntax">Search query syntax</a>
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used here
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> loadApplicationObjectsSearch(String search, Callback callback, CMRequestOptions options) {
        return applicationService.asyncSearch(search, objectLoadUpdateStoreCallback(callback, StoreIdentifier.applicationLevel()), options);
    }

    /**
     * Retrieve all the user level objects that are of the specified class. Class values are set using
     * {@link SimpleCMObject#setClass(String)}
     * @param klass the class type to load
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void loadUserObjectsOfClass(String klass) throws CreationException {
        loadUserObjectsOfClass(klass, Callback.DO_NOTHING);
    }

    /**
     * Retrieve all the user level objects that are of the specified class. Class values are set using
     * {@link SimpleCMObject#setClass(String)}
     * @param klass the class type to load
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used here
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void loadUserObjectsOfClass(String klass, Callback callback) throws CreationException {
        loadUserObjectsOfClass(klass, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the user level objects that are of the specified class. Class values are set using
     * {@link SimpleCMObject#setClass(String)}
     * @param klass the class type to load
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used here
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void loadUserObjectsOfClass(final String klass, final Callback callback, final CMRequestOptions options) throws CreationException {
        user().login(new LoginResponseCallback() {
           public void onCompletion(LoginResponse response) {
               userService().asyncLoadObjectsOfClass(klass, objectLoadUpdateStoreCallback(callback, StoreIdentifier.StoreIdentifier(user())), options);
           }
        });

    }

    /**
     * Retrieve all the application level objects that are of the specified class. Class values are set using
     * {@link SimpleCMObject#setClass(String)}
     * @param klass the class type to load
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used here
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> loadApplicationObjectsOfClass(String klass, Callback callback) {
        return loadApplicationObjectsOfClass(klass, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the application level objects that are of the specified class. Class values are set using
     * {@link SimpleCMObject#setClass(String)}
     * @param klass the class type to load
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used here
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> loadApplicationObjectsOfClass(String klass, Callback callback, CMRequestOptions options) {
        return applicationService.asyncLoadObjectsOfClass(klass, objectLoadUpdateStoreCallback(callback, StoreIdentifier.applicationLevel()), options);
    }

    /**
     * Saves all the application level objects that were added using {@link #addObject(com.cloudmine.api.SimpleCMObject)}
     * Note that the object level check occurs on save, not on insertion, so if an object is added and then the object level is
     * modified, it will be saved using the new object level
     * @throws JsonConversionException If unable to convert one of the application level objects to json; this should never happen through normal usage
     */
    public void saveStoreApplicationObjects() throws JsonConversionException {
        saveStoreApplicationObjects(Callback.DO_NOTHING);
    }

    /**
     * Saves all the application level objects that were added using {@link #addObject(com.cloudmine.api.SimpleCMObject)}
     * Note that the object level check occurs on save, not on insertion, so if an object is added and then the object level is
     * modified, it will be saved using the new object level
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @throws JsonConversionException If unable to convert one of the application level objects to json; this should never happen through normal usage
     */
    public void saveStoreApplicationObjects(Callback callback) throws JsonConversionException {
        saveStoreApplicationObjects(callback, CMRequestOptions.NONE);
    }

    /**
     * Saves all the application level objects that were added using {@link #addObject(com.cloudmine.api.SimpleCMObject)}
     * Note that the object level check occurs on save, not on insertion, so if an object is added and then the object level is
     * modified, it will be saved using the new object level
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @throws JsonConversionException If unable to convert one of the objects to json; this should never happen through normal usage
     */
    public void saveStoreApplicationObjects(Callback callback, CMRequestOptions options) throws JsonConversionException {
        applicationService.asyncInsert(getStoreObjectsOfType(ObjectLevel.APPLICATION), callback, options);
    }

    /**
     * Saves all the user level objects that were added using {@link #addObject(com.cloudmine.api.SimpleCMObject)}
     * Note that the object level check occurs on save, not on insertion, so if an object is added and then the object level is
     * modified, it will be saved using the new object level
     * @throws JsonConversionException If unable to convert one of the objects to json; this should never happen through normal usage
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void saveStoreUserObjects() throws JsonConversionException, CreationException {
        saveStoreUserObjects(Callback.DO_NOTHING);
    }

    /**
     * Saves all the user level objects that were added using {@link #addObject(com.cloudmine.api.SimpleCMObject)}
     * Note that the object level check occurs on save, not on insertion, so if an object is added and then the object level is
     * modified, it will be saved using the new object level
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @throws JsonConversionException If unable to convert one of the objects to json; this should never happen through normal usage
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void saveStoreUserObjects(Callback callback) throws JsonConversionException, CreationException {
        saveStoreUserObjects(callback, CMRequestOptions.NONE);
    }

    /**
     * Saves all the user level objects that were added using {@link #addObject(com.cloudmine.api.SimpleCMObject)}
     * Note that the object level check occurs on save, not on insertion, so if an object is added and then the object level is
     * modified, it will be saved using the new object level
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @throws JsonConversionException If unable to convert one of the application level objects to json; this should never happen through normal usage
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void saveStoreUserObjects(final Callback callback, final CMRequestOptions options) throws JsonConversionException, CreationException {
        user().login(new LoginResponseCallback() {
           public void onCompletion(LoginResponse response) {
               userService().asyncInsert(getStoreObjectsOfType(ObjectLevel.USER), callback, options);
           }
        });
    }

    /**
     * Saves all the objects that were added using {@link #addObject(com.cloudmine.api.SimpleCMObject)} to their specified level
     * Note that the object level check occurs on save, not on insertion, so if an object is added and then the object level is
     * modified, it will be saved using the new object level
     * Note that this method makes two calls to the CloudMine API; once for application level, once for user level
     * @throws JsonConversionException If unable to convert one of the application level objects to json; this should never happen through normal usage
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it and at least one object to be saved has a {@link ObjectLevel#USER}
     */
    public void saveStoreObjects() throws JsonConversionException, CreationException {
        saveStoreObjects(Callback.DO_NOTHING);
    }

    /**
     * Saves all the objects that were added using {@link #addObject(com.cloudmine.api.SimpleCMObject)} to their specified level
     * Note that the object level check occurs on save, not on insertion, so if an object is added and then the object level is
     * modified, it will be saved using the new object level
     * Note that this method makes two calls to the CloudMine API; once for application level, once for user level
     * @param appCallback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this. This will get the application level results
     * @param userCallback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this. This will get the user level results
     * @throws JsonConversionException If unable to convert one of the application level objects to json; this should never happen through normal usage
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it and at least one object to be saved has a {@link ObjectLevel#USER}
     */
    public void saveStoreObjects(Callback appCallback, Callback userCallback) throws JsonConversionException, CreationException {
        saveStoreObjects(appCallback, userCallback, CMRequestOptions.NONE);
    }

    /**
     * Saves all the objects that were added using {@link #addObject(com.cloudmine.api.SimpleCMObject)} to their specified level
     * Note that the object level check occurs on save, not on insertion, so if an object is added and then the object level is
     * modified, it will be saved using the new object level
     * Note that this method makes two calls to the CloudMine API; once for application level, once for user level
     * @param appCallback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this. This will get the application level results
     * @param userCallback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this. This will get the user level results
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @throws JsonConversionException If unable to convert one of the application level objects to json; this should never happen through normal usage
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it and at least one object to be saved has a {@link ObjectLevel#USER}
     */
    public void saveStoreObjects(Callback appCallback, Callback userCallback, CMRequestOptions options) throws JsonConversionException, CreationException {
        saveStoreUserObjects(userCallback, options);
        saveStoreApplicationObjects(appCallback, options);
    }


    /**
     * Saves all the objects that were added using {@link #addObject(com.cloudmine.api.SimpleCMObject)} to their specified level
     * Note that the object level check occurs on save, not on insertion, so if an object is added and then the object level is
     * modified, it will be saved using the new object level
     * Note that this method makes two calls to the CloudMine API; once for application level, once for user level
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this. This will be called twice; once for the application results, once with the user results
     * @throws JsonConversionException If unable to convert one of the application level objects to json; this should never happen through normal usage
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it and at least one object to be saved has a {@link ObjectLevel#USER}
     */
    public void saveStoreObjects(Callback callback) throws JsonConversionException, CreationException {
        saveStoreObjects(callback, CMRequestOptions.NONE);
    }

    /**
     * Saves all the objects that were added using {@link #addObject(com.cloudmine.api.SimpleCMObject)} to their specified level
     * Note that the object level check occurs on save, not on insertion, so if an object is added and then the object level is
     * modified, it will be saved using the new object level
     * Note that this method makes two calls to the CloudMine API; once for application level, once for user level
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this. This will be called twice; once for the application results, once with the user results
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @throws JsonConversionException If unable to convert one of the application level objects to json; this should never happen through normal usage
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it and at least one object to be saved has a {@link ObjectLevel#USER}
     */
    public void saveStoreObjects(Callback callback, CMRequestOptions options) throws JsonConversionException, CreationException {
        saveStoreObjects(callback, callback, options);
    }

//    public void saveStoreObjects(final Callback callback) {
//        //TODO this is a messy implementation. Basically do both inserts and start a thread that waits for the result
//        //there is a much better way to do it but I don't have time to figure it out right now #excuses #shipit
//        final CountDownLatch latch = new CountDownLatch(2);
//        final List<ObjectModificationResponse> responses = new ArrayList<ObjectModificationResponse>();
//        ObjectModificationResponseCallback countDownCallback = new ObjectModificationResponseCallback() {
//            public void onCompletion(ObjectModificationResponse response) {
//                responses.add(response);
//                latch.countDown();
//            }
//        };
//        saveStoreUserObjects(countDownCallback);
//        saveStoreApplicationObjects(countDownCallback);
//        Runnable toRun = new Runnable() {
//
//            @Override
//            public void run() {
//                ObjectModificationResponse response = null;
//                try {
//                    latch.await();
//                    response = ObjectModificationResponse.merge(responses);
//
//                } catch (InterruptedException e) {
//                } finally {
//                    if(response == null) {
//                        response = new ObjectModificationResponse(EMPTY_SUCCESS_RESPONSE, 408);
//                    }
//                    callback.onCompletion(response);
//                }
//
//            }
//        };
//        new Thread(toRun).start();
//    }

    private Collection<SimpleCMObject> getStoreObjectsOfType(ObjectLevel level) {
        List<SimpleCMObject> storeObjects = new ArrayList<SimpleCMObject>();
        for(SimpleCMObject object : objects.values()) {
            if(object.isOnLevel(level)) {
                if((ObjectLevel.APPLICATION == level && object.isOnLevel(ObjectLevel.UNKNOWN))) {
                    object.setSaveWith(StoreIdentifier.applicationLevel());
                }
                storeObjects.add(object);
            }
        }
        return storeObjects;
    }

    /**
     * Add the specified object to the store. No API calls are performed as a result of this operation; to
     * save the added object, call {@link #saveStoreObjects()} or a related method
     * @param object gets added to the local store
     */
    public void addObject(SimpleCMObject object) {
        objects.put(object.getObjectId(), object);
    }

    /**
     * Add all the given objects to the store. No API calls are performed as a result of this operation
     * @param objects to add to the local store
     */
    public void addObjects(Collection<SimpleCMObject> objects) {
        if(objects == null) {
            return;
        }
        for(SimpleCMObject object : objects) {
            addObject(object);
        }
    }

    /**
     * Remove the specified object from the store. No API calls are performed as a result of this operation
     * @param object gets removed from the local store
     */
    public void removeObject(SimpleCMObject object) {
        if(object == null)
            return;
        removeObject(object.getObjectId());
    }

    /**
     * Remove the object specified by this objectId from the store. No API calls are performed as a result of this operation
     * @param objectId the id of the object to remove from the store
     */
    public void removeObject(String objectId) {
        objects.remove(objectId);
    }

    /**
     * Remove all the objects with the given objectIds from the store. No API calls are performed as a result of this operation
     * @param objectIds the ids of the objects to remove from the store
     */
    public void removeObjects(Collection<String> objectIds) {
        if(objectIds == null) {
            return;
        }
        for(String objectId : objectIds) {
            removeObject(objectId);
        }
    }

    /**
     * Retrieve any existing, added SimpleCMObject with the specified objectId
     * @param objectId the objectId associated with the desired SimpleCMObject
     * @return the SimpleCMObject if it exists; null otherwise
     */
    public SimpleCMObject getStoredObject(String objectId) {
        if(objectId == null) {
            return null;
        }
        return objects.get(objectId);
    }

    /**
     * Get all of the objects that have been persisted using this store
     * @return all of the objects that have been persisted using this store
     */
    public List<SimpleCMObject> getStoredObjects() {
        return new ArrayList<SimpleCMObject>(objects.values());
    }

    /**********************************FILES******************************/

    /**
     * Save the given file asynchronously with its set StoreIdentifier. If StoreIdentifier has not been set, default (application) level is used
     * @param file the file to save
     * @throws CreationException If this CMStore does not have a CMUser associated with it
     */
    public void saveFile(CMFile file) throws CreationException {
        saveFile(file, Callback.DO_NOTHING);
    }

    /**
     * Save the given file asynchronously with its set StoreIdentifier. If StoreIdentifier has not been set, default (application) level is used
     * @param file the file to save. Must not be null
     * @param callback will be called on completion; expects a FileCreationResponse, it is recommended a {@link com.cloudmine.api.rest.callbacks.FileCreationResponseCallback} is used here
     * @throws CreationException If this CMStore does not have a CMUser associated with it
     */
    public void saveFile(final CMFile file, final Callback callback) throws CreationException {
        if(file.isOnLevel(ObjectLevel.USER)) {
            login(user(), new LoginResponseCallback() {
                @Override
                public void onCompletion(LoginResponse response) {
                    userService().asyncUpload(file, callback);
                }
            });
        } else {
            file.setSaveWith(StoreIdentifier.applicationLevel());
            applicationService.asyncUpload(file, callback);
        }
    }

    /**
     * Retrieve the {@link CMFile} with the specified fileName, if it exists at the application level
     * @param fileName the file fileName, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @return a Future containing the {@link FileLoadResponse}
     */
    public Future<FileLoadResponse> loadApplicationFile(String fileName) {
        return loadApplicationFile(fileName, Callback.DO_NOTHING);
    }

    /**
     * Retrieve the {@link CMFile} with the specified fileName, if it exists at the application level
     * @param fileName the file fileName, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects a FileLoadResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.FileLoadCallback} is passed in
     * @return a Future containing the {@link FileLoadResponse}
     */
    public Future<FileLoadResponse> loadApplicationFile(String fileName, Callback callback) {
        return loadApplicationFile(fileName, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve the {@link CMFile} with the specified fileName, if it exists at the application level
     * @param fileName the file fileName, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects a FileLoadResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.FileLoadCallback} is passed in
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     * @return a Future containing the {@link FileLoadResponse}
     */
    public Future<FileLoadResponse> loadApplicationFile(String fileName, Callback callback, CMRequestOptions options) {
        return applicationService.asyncLoadFile(fileName, callback, options);
    }

    /**
     * Retrieve the {@link CMFile} with the specified fileName, if it exists at the user level
     * @param fileName the file fileName, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void loadUserFile(String fileName) throws CreationException {
        loadUserFile(fileName, Callback.DO_NOTHING);
    }

    /**
     * Retrieve the {@link CMFile} with the specified fileName, if it exists at the user level
     * @param fileName the file fileName, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects a FileLoadResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.FileLoadCallback} is passed in
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void loadUserFile(String fileName, Callback callback) throws CreationException {
        loadUserFile(fileName, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve the {@link CMFile} with the specified fileName, if it exists at the user level
     * @param fileName the file fileName, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects a FileLoadResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.FileLoadCallback} is passed in
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void loadUserFile(final String fileName, final Callback callback, final CMRequestOptions options) throws CreationException {
        user().login(new LoginResponseCallback() {
           public void onCompletion(LoginResponse response) {
               userService().asyncLoadFile(fileName, callback, options);
           }
        });
    }

    /**
     * Delete the {@link CMFile} with the specified fileName, if it exists at the application level
     * @param fileName the file fileName, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @return a Future containing the {@link ObjectModificationResponse}
     */
    public Future<ObjectModificationResponse> deleteApplicationFile(String fileName) {
        return deleteApplicationFile(fileName, Callback.DO_NOTHING);
    }

    /**
     * Delete the {@link CMFile} with the specified fileName, if it exists at the application level
     * @param fileName the file fileName, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link ObjectModificationResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in
     * @return a Future containing the {@link ObjectModificationResponse}
     */
    public Future<ObjectModificationResponse> deleteApplicationFile(String fileName, Callback callback) {
        return deleteApplicationFile(fileName, callback, CMRequestOptions.NONE);
    }

    /**
     * Delete the {@link CMFile} with the specified fileName, if it exists at the application level
     * @param fileName the file fileName, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link ObjectModificationResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     * @return a Future containing the {@link ObjectModificationResponse}
     */
    public Future<ObjectModificationResponse> deleteApplicationFile(String fileName, Callback callback, CMRequestOptions options) {
        return applicationService.asyncDeleteFile(fileName, callback, options);
    }

    /**
     * Delete the {@link CMFile} with the specified fileName, if it exists at the user level
     * @param fileName the file fileName, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void deleteUserFile(String fileName) throws CreationException {
        deleteUserFile(fileName, Callback.DO_NOTHING);
    }

    /**
     * Delete the {@link CMFile} with the specified fileName, if it exists at the user level
     * @param fileName the file fileName, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link ObjectModificationResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void deleteUserFile(String fileName, Callback callback) throws CreationException {
        deleteUserFile(fileName, callback, CMRequestOptions.NONE);
    }

    /**
     * Delete the {@link CMFile} with the specified fileName, if it exists at the user level.
     * @param fileName the file fileName, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link ObjectModificationResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void deleteUserFile(final String fileName, final Callback callback, final CMRequestOptions options) throws CreationException {
        user().login(new LoginResponseCallback() {
            public void onCompletion(LoginResponse response) {
                userService().asyncDeleteFile(fileName, callback, options);
            }
        });
    }

    /*********************************USERS*******************************/
    private UserCMWebService userService() throws CreationException {
        return applicationService.getUserWebService(user().getSessionToken());
    }

    /**
     * Log in the specified user and set the {@link com.cloudmine.api.CMUser} for this store
     * @param user the user to log in
     * @return a Future containing the {@link LoginResponse}
     */
    public Future<LoginResponse> login(CMUser user) {
        return login(user, Callback.DO_NOTHING);
    }

    /**
     * Log in the specified user and set the {@link com.cloudmine.api.CMUser} for this store
     * @param user the user to log in
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link LoginResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.LoginResponseCallback} is passed in
     * @return a Future containing the {@link LoginResponse}
     */
    public Future<LoginResponse> login(CMUser user, Callback callback) {
        setUser(user);
        return user.login(callback);
    }

    /**
     * Sets the user. Can only be called once per store per user; subsequant calls are ignored
     * If you log in via the store, calling this method is unnecessary.
     * @param user a user; if the user is not logged in, they will be before any
     * @return true if the user value was set; false if it has already been set or a null user was given
     */
    public boolean setUser(CMUser user) {
        return this.user.setValue(user);
    }

}

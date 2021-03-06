package com.cloudmine.api.rest;

import com.cloudmine.api.*;
import com.cloudmine.api.exceptions.AccessException;
import com.cloudmine.api.exceptions.ConversionException;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.callbacks.CMCallback;
import com.cloudmine.api.rest.callbacks.CMObjectResponseCallback;
import com.cloudmine.api.rest.callbacks.Callback;
import com.cloudmine.api.rest.callbacks.ExceptionPassthroughCallback;
import com.cloudmine.api.rest.options.CMRequestOptions;
import com.cloudmine.api.rest.response.*;

import javax.security.auth.login.LoginException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The main class for interacting with the CloudMine API. Stores can operate on both the user or application level
 * Preconditions for use:<br>
 * {@link com.cloudmine.api.BaseDeviceIdentifier#initialize(android.content.Context)} has been called with the activity context<br>
 * {@link CMApiCredentials#initialize(String, String)} has been called with the application identifier and API key<br>
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 * @deprecated not the best model for Android specific development. Volley requests should be used instead
 */
@Deprecated
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
    public static CMStore getStore(JavaCMUser user) throws CreationException {
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
    private final Immutable<JavaCMUser> user = new Immutable<JavaCMUser>();
    private final Map<String, CMObject> objects = new ConcurrentHashMap<String, CMObject>();

    private CMStore(StoreIdentifier identifier) throws CreationException {
        if(identifier == null) {
            identifier = StoreIdentifier.DEFAULT;
        }
        if(identifier.isUserLevel()) {
            setUser(identifier.getUser());
        }
        applicationService = CMWebService.getService();
    }

    private JavaCMUser user() {
        return user.valueOrThrow();
    }

    private final Callback<CMObjectResponse> objectLoadUpdateStoreCallback(final Callback<CMObjectResponse> callback, final StoreIdentifier identifier) {
        return new ExceptionPassthroughCallback<CMObjectResponse>(callback) {
            public void onCompletion(CMObjectResponse response) {
                try {
                    if(response.wasSuccess()) {
                        List<CMObject> cmObjects = response.getObjects();
                        addObjects(cmObjects);
                        for(CMObject object : cmObjects) {
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

    public void saveAccessList(JavaAccessListController list) {
        saveAccessList(list, CMCallback.<CreationResponse>doNothing());
    }

    /**
     *
     * @param list the list to save
     * @param callback expects a {@link com.cloudmine.api.rest.response.CreationResponse}, recommended that you use a {@link com.cloudmine.api.rest.callbacks.CreationResponseCallback}
     */
    public void saveAccessList(final JavaAccessListController list, final Callback<CreationResponse> callback) {
        final JavaCMUser listUser = list.getUser();
        if(listUser.isLoggedIn()) {
            CMWebService.getService().getUserWebService(listUser.getSessionToken()).asyncInsert(list, callback);
        } else {
            listUser.login(new ExceptionPassthroughCallback<LoginResponse>(callback) {
                public void onCompletion(LoginResponse response) {
                    if(listUser.isLoggedIn()) {
                        CMWebService.getService().getUserWebService(listUser.getSessionToken()).asyncInsert(list, callback);
                    } else {
                        callback.onFailure(new LoginException("Cannot save ACL belonging to user who cannot log in"), response.getMessageBody());
                    }
                }
            });
        }

    }

    /**
     * Asynchronously save the object based on the StoreIdentifier associated with it. If no StoreIdentifier is
     * present, default (app level) is used; however, the object's StoreIdentifier is not updated.
     * NOTE: No matter what user is associated with the object to save, the store always saves the object with the user associated with the store.
     * @param object the object to save
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void saveObject(CMObject object) throws ConversionException, CreationException {
        saveObject(object, CMCallback.<ObjectModificationResponse>doNothing());
    }

    /**
     * Asynchronously save the object based on the StoreIdentifier associated with it. If no StoreIdentifier is
     * present, default (app level) is used; however, the object's StoreIdentifier is not updated.
     * NOTE: No matter what user is associated with the object to save, the store always saves the object with the user associated with the store.
     * @param object the object to save
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it and this object is UserLevel
     */
    public void saveObject(CMObject object, Callback<ObjectModificationResponse> callback) throws ConversionException, CreationException {
        saveObject(object, callback, CMRequestOptions.NONE);
    }

    /**
     * Asynchronously save the object based on the StoreIdentifier associated with it. If no StoreIdentifier is
     * present, default (app level) is used; and the object's StoreIdentifier is updated.
     * NOTE: No matter what user is associated with the object to save, the store always saves the object with the user associated with the store.
     * @param object the object to save
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void saveObject(final CMObject object, final Callback<ObjectModificationResponse> callback, final CMRequestOptions options) throws ConversionException, CreationException {
        addObject(object);
        if(object.isOnLevel(ObjectLevel.USER)) {
            user().login(new ExceptionPassthroughCallback<LoginResponse>(callback) {
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
    public void deleteObject(CMObject object) throws CreationException {
        deleteObject(object, CMCallback.<ObjectModificationResponse>doNothing());
    }

    /**
     * Delete the given object from CloudMine. If no StoreIdentifier is present, default (app level) is
     * used; however, the object's StoreIdentifier is not updated.
     * NOTE: No matter what user is associated with the object to save, the store always deletes the object with the user associated with the store.
     * @param object to delete; this is done based on the object id, its values are ignored
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it and you are deleting a User level object
     */
    public void deleteObject(CMObject object, Callback<ObjectModificationResponse> callback) throws CreationException {
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
    public void deleteObject(final CMObject object, final Callback<ObjectModificationResponse> callback, final CMRequestOptions options) throws CreationException {
        removeObject(object);
        if(object.isOnLevel(ObjectLevel.USER)) {
            user().login(new ExceptionPassthroughCallback<LoginResponse>(callback) {
                public void onCompletion(LoginResponse response) {
                    userService().asyncDeleteObject(object, callback, options);
                }
            });
        } else {
            applicationService.asyncDeleteObject(object, callback, options);
        }
    }

    /**
     * Retrieve all the application level objects; they will be added to this Store after load
     */
    public void loadAllApplicationObjects() {
        loadAllApplicationObjects(CMCallback.<CMObjectResponse>doNothing());
    }

    /**
     * Retrieve all the application level objects and pass the results into the given callback. They will be added to this Store after load
     * @param callback a Callback that expects a {@link com.cloudmine.api.rest.response.CMObjectResponse}. It is recommended that a {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used
     */
    public void loadAllApplicationObjects(Callback<CMObjectResponse> callback) {
        loadAllApplicationObjects(callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the application level objects and pass the results into the given callback. They will be added to this Store after load
     * @param callback a Callback that expects a {@link com.cloudmine.api.rest.response.CMObjectResponse}. It is recommended that a {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     */
    public void loadAllApplicationObjects(Callback<CMObjectResponse> callback, CMRequestOptions options) {
        applicationService.asyncLoadObjects(objectLoadUpdateStoreCallback(callback, StoreIdentifier.DEFAULT),
                options);
    }

    /**
     * Retrieve all the application level objects; they will be added to this Store after load
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void loadAllUserObjects() throws CreationException {
        loadAllUserObjects(CMCallback.<CMObjectResponse>doNothing());
    }

    /**
     * Retrieve all the user level objects and pass the results into the given callback; they will be added to this Store after load
     * @param callback a Callback that expects a {@link com.cloudmine.api.rest.response.CMObjectResponse}. It is recommended that a {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void loadAllUserObjects(Callback<CMObjectResponse> callback) throws CreationException {
        loadAllUserObjects(callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the user level objects and pass the results into the given callback; they will be added to this Store after load
     * @param callback a Callback that expects a {@link com.cloudmine.api.rest.response.CMObjectResponse}. It is recommended that a {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @throws CreationException if this CMStore does not have a CMUser associated with it
     */
    public void loadAllUserObjects(final Callback<CMObjectResponse> callback, final CMRequestOptions options) throws CreationException {
        user().login(new ExceptionPassthroughCallback<LoginResponse>(callback) {
           public void onCompletion(LoginResponse response) {
               userService().asyncLoadObjects(objectLoadUpdateStoreCallback(callback, StoreIdentifier.StoreIdentifier(user())), options);
           }
        });
    }

    /**
     * Retrieve all the application level objects with the given objectIds; they will be added to this Store after load
     * @param objectIds the top level objectIds of the objects to retrieve
     */
    public void loadApplicationObjectsWithObjectIds(Collection<String> objectIds) {
        loadApplicationObjectsWithObjectIds(objectIds, CMCallback.<CMObjectResponse>doNothing());
    }

    /**
     * Retrieve all the application level objects with the given objectIds; they will be added to this Store after load
     * @param objectIds the top level objectIds of the objects to retrieve
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used here
     */
    public void loadApplicationObjectsWithObjectIds(Collection<String> objectIds, Callback<CMObjectResponse> callback) {
        loadApplicationObjectsWithObjectIds(objectIds, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the application level objects with the given objectIds; they will be added to this Store after load
     * @param objectIds the top level objectIds of the objects to retrieve
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used here
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     */
    public void loadApplicationObjectsWithObjectIds(Collection<String> objectIds, Callback<CMObjectResponse> callback, CMRequestOptions options) {
        applicationService.asyncLoadObjects(objectIds, objectLoadUpdateStoreCallback(callback, StoreIdentifier.applicationLevel()), options);
    }

    /**
     * Retrieve all the application level objects with the given objectIds; they will be added to this Store after load
     * @param objectId the top level objectIds of the objects to retrieve
     */
    public void loadApplicationObjectWithObjectId(String objectId) {
        loadApplicationObjectWithObjectId(objectId, CMCallback.<CMObjectResponse>doNothing());
    }
    /**
     * Retrieve all the application level objects with the given objectIds; they will be added to this Store after load
     * @param objectId the top level objectIds of the objects to retrieve
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used here
     */
    public void loadApplicationObjectWithObjectId(String objectId, Callback<CMObjectResponse> callback) {
        loadApplicationObjectWithObjectId(objectId, callback, CMRequestOptions.NONE);
    }
    /**
     * Retrieve all the application level objects with the given objectIds; they will be added to this Store after load
     * @param objectId the top level objectIds of the objects to retrieve
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used here
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     */
    public void loadApplicationObjectWithObjectId(String objectId, Callback<CMObjectResponse> callback, CMRequestOptions options) {
        applicationService.asyncLoadObject(objectId, objectLoadUpdateStoreCallback(callback, StoreIdentifier.applicationLevel()), options);
    }

    /**
     * Retrieve all the user level objects with the given objectIds; they will be added to this Store after load
     * @param objectIds the top level objectIds of the objects to retrieve
     * @throws CreationException if this CMStore does not have a CMUser associated with it
     */
    public void loadUserObjectsWithObjectIds(Collection<String> objectIds) throws CreationException {
        loadUserObjectsWithObjectIds(objectIds, CMCallback.<CMObjectResponse>doNothing());
    }

    /**
     * Retrieve all the user level objects with the given objectIds; they will be added to this Store after load
     * @param objectIds the top level objectIds of the objects to retrieve
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used here
     * @throws CreationException if this CMStore does not have a CMUser associated with it
     */
    public void loadUserObjectsWithObjectIds(Collection<String> objectIds, Callback<CMObjectResponse> callback) throws CreationException {
        loadUserObjectsWithObjectIds(objectIds, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the user level objects with the given objectIds; they will be added to this Store after load
     * @param objectIds the top level objectIds of the objects to retrieve
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used here
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @throws CreationException if this CMStore does not have a CMUser associated with it
     */
    public void loadUserObjectsWithObjectIds(final Collection<String> objectIds, final Callback<CMObjectResponse> callback, final CMRequestOptions options) throws CreationException {
        user().login(new ExceptionPassthroughCallback<LoginResponse>(callback) {
            public void onCompletion(LoginResponse response) {
                userService().asyncLoadObjects(objectIds, objectLoadUpdateStoreCallback(callback, StoreIdentifier.StoreIdentifier(user())), options);
            }
        });
    }

    /**
     * Retrieve all the user level objects that match the given search; they will be added to this Store after load
     * @param search the search string to use. For more information on syntax. See <a href="https://cloudmine.me/docs/object-storage#query_syntax">Search query syntax</a>
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void loadUserObjectsSearch(String search) throws CreationException {
        loadUserObjectsSearch(search, CMCallback.<CMObjectResponse>doNothing());
    }

    /**
     * Retrieve all the user level objects that match the given search; they will be added to this Store after load
     * @param search the search string to use. For more information on syntax. See <a href="https://cloudmine.me/docs/object-storage#query_syntax">Search query syntax</a>
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used here
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void loadUserObjectsSearch(String search, Callback<CMObjectResponse> callback) throws CreationException {
        loadUserObjectsSearch(search, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the user level objects that match the given search; they will be added to this Store after load
     * @param search the search string to use. For more information on syntax. See <a href="https://cloudmine.me/docs/object-storage#query_syntax">Search query syntax</a>
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used here
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void loadUserObjectsSearch(final String search, final Callback<CMObjectResponse> callback, final CMRequestOptions options) throws CreationException {
        user().login(new ExceptionPassthroughCallback<LoginResponse>(callback) {
            @Override
            public void onCompletion(LoginResponse response) {
                userService().asyncSearch(search, objectLoadUpdateStoreCallback(callback, StoreIdentifier.StoreIdentifier(user())), options);
            }
        });
    }

    /**
     * Retrieve all the application level objects that match the given search; they will be added to this Store after load
     * @param search the search string to use. For more information on syntax. See <a href="https://cloudmine.me/docs/object-storage#query_syntax">Search query syntax</a>
     */
    public void loadApplicationObjectsSearch(String search) {
        loadApplicationObjectsSearch(search, CMCallback.<CMObjectResponse>doNothing());
    }

    /**
     * Retrieve all the application level objects that match the given search; they will be added to this Store after load
     * @param search the search string to use. For more information on syntax. See <a href="https://cloudmine.me/docs/object-storage#query_syntax">Search query syntax</a>
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used here
     */
    public void loadApplicationObjectsSearch(String search, Callback<CMObjectResponse> callback) {
        loadApplicationObjectsSearch(search, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the application level objects that match the given search; they will be added to this Store after load
     * @param search the search string to use. For more information on syntax. See <a href="https://cloudmine.me/docs/object-storage#query_syntax">Search query syntax</a>
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used here
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     */
    public void loadApplicationObjectsSearch(String search, Callback<CMObjectResponse> callback, CMRequestOptions options) {
        applicationService.asyncSearch(search, objectLoadUpdateStoreCallback(callback, StoreIdentifier.applicationLevel()), options);
    }



    /**
     * Retrieve all the user level objects that are of the specified class. Class values are determined automatically, or
     * can be set by overriding {@link com.cloudmine.api.CMObject#getClassName()}. retrieved objects will be added to this Store after load
     * @param klass the class type to load
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void loadUserObjectsOfClass(String klass) throws CreationException {
        loadUserObjectsOfClass(klass, CMCallback.<CMObjectResponse>doNothing());
    }

    /**
     * Retrieve all the user level objects that are of the specified class; they will be added to this Store after load.
     * Class values are determined automatically, or can be set by overriding {@link com.cloudmine.api.CMObject#getClassName()}
     * @param klass the class type to load
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used here
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void loadUserObjectsOfClass(String klass, Callback<CMObjectResponse> callback) throws CreationException {
        loadUserObjectsOfClass(klass, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the user level objects that are of the specified class; they will be added to this Store after load.
     * Class values are determined automatically, or can be set by overriding {@link com.cloudmine.api.CMObject#getClassName()}
     * @param klass the class type to load
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used here
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void loadUserObjectsOfClass(final String klass, final Callback<CMObjectResponse> callback, final CMRequestOptions options) throws CreationException {
        user().login(new ExceptionPassthroughCallback<LoginResponse>(callback) {
            public void onCompletion(LoginResponse response) {
                userService().asyncLoadObjectsOfClass(klass, objectLoadUpdateStoreCallback(callback, StoreIdentifier.StoreIdentifier(user())), options);
            }
        });
    }

    /**
     * See {@link #loadUserObjectsOfClass(String, com.cloudmine.api.rest.callbacks.Callback)}
     * @param klass
     * @param callback
     * @param <CMO>
     */
    public <CMO extends CMObject> void loadUserObjectsOfClass(final Class<CMO> klass, final Callback<CMObjectResponse> callback) {
        loadUserObjectsOfClass(klass, CMRequestOptions.NONE, callback);
    }

    /**
     * See {@link #loadUserObjectsOfClass(String, com.cloudmine.api.rest.callbacks.Callback, com.cloudmine.api.rest.options.CMRequestOptions)}
     * @param klass
     * @param options
     * @param callback
     * @param <CMO>
     */
    public <CMO extends CMObject> void loadUserObjectsOfClass(final Class<CMO> klass, final CMRequestOptions options, final Callback<CMObjectResponse> callback) {
        user().login(new ExceptionPassthroughCallback<LoginResponse>(callback) {
            public void onCompletion(LoginResponse response) {
                userService().asyncLoadObjectsOfClass(klass, objectLoadUpdateStoreCallback(callback, StoreIdentifier.StoreIdentifier(user())), options);
            }
        });
    }

    /**
     * See {@link #loadUserObjectsOfClassWithSearch(Class, String, com.cloudmine.api.rest.options.CMRequestOptions, com.cloudmine.api.rest.callbacks.Callback)}
     * @param klass
     * @param search
     * @param callback
     * @param <CMO>
     */
    public <CMO extends CMObject> void loadUserObjectsOfClassWithSearch(final Class<CMO> klass, final String search, final Callback<CMObjectResponse> callback) {
        loadUserObjectsOfClassWithSearch(klass, search, CMRequestOptions.NONE, callback);
    }

    /**
     * Load user objects of the specific class, filtered by the given search. Recommended that a {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback}
     * is used here, so the returned objects will be accessable without having to be cast
     * @param klass the Class of the objects to load
     * @param search any additional filters, must conform to the CloudMine syntax for searching (ie, "[field = "value"]"
     * @param options any additional options for the query
     * @param callback recommended a {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is passed here
     * @param <CMO> the type of the objects being loaded
     */
    public <CMO extends CMObject> void loadUserObjectsOfClassWithSearch(final Class<CMO> klass, final String search, final CMRequestOptions options, final Callback<CMObjectResponse> callback) {
        user().login(new ExceptionPassthroughCallback<LoginResponse>(callback) {
            public void onCompletion(LoginResponse response) {
                userService().asyncLoadObjectsOfClassAndSearch(klass, search, objectLoadUpdateStoreCallback(callback, StoreIdentifier.StoreIdentifier(user())), options);
            }
        });
    }

    /**
     * Retrieve all the application level objects that are of the specified class; they will be added to this Store after load.
     * Class values are determined automatically, or
     * can be set by overriding {@link com.cloudmine.api.CMObject#getClassName()}
     * @param klass the class type to load
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used here
     */
    public void loadApplicationObjectsOfClass(String klass, Callback<CMObjectResponse> callback) {
        loadApplicationObjectsOfClass(klass, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the application level objects that are of the specified class; they will be added to this Store after load.
     * Class values are determined automatically, or
     * can be set by overriding {@link com.cloudmine.api.CMObject#getClassName()}
     * @param klass the class type to load
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used here
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     */
    public void loadApplicationObjectsOfClass(String klass, Callback<CMObjectResponse> callback, CMRequestOptions options) {
        applicationService.asyncLoadObjectsOfClass(klass, objectLoadUpdateStoreCallback(callback, StoreIdentifier.applicationLevel()), options);
    }

    /**
     * See {@link #loadApplicationObjectsOfClass(String, com.cloudmine.api.rest.callbacks.Callback, com.cloudmine.api.rest.options.CMRequestOptions)}
     * @param klass
     * @param callback
     * @param <CMO>
     */
    public <CMO extends CMObject> void loadApplicationObjectsOfClass(Class<CMO> klass, Callback<CMObjectResponse> callback) {
        loadApplicationObjectsOfClass(klass, CMRequestOptions.NONE, callback);
    }

    /**
     * See {@link #loadApplicationObjectsOfClass(String, com.cloudmine.api.rest.callbacks.Callback, com.cloudmine.api.rest.options.CMRequestOptions)}
     * @param klass
     * @param options
     * @param callback
     * @param <CMO>
     */
    public <CMO extends CMObject> void loadApplicationObjectsOfClass(Class<CMO> klass, CMRequestOptions options, Callback<CMObjectResponse> callback) {
        applicationService.asyncLoadObjectsOfClass(klass, callback, options);
    }

    /**
     * See {@link #loadApplicationObjectsOfClassWithSearch(Class, String, com.cloudmine.api.rest.options.CMRequestOptions, com.cloudmine.api.rest.callbacks.Callback)}
     * @param klass
     * @param search
     * @param callback
     * @param <CMO>
     */
    public <CMO extends CMObject> void loadApplicationObjectsOfClassWithSearch(Class<CMO> klass, String search, Callback<CMObjectResponse> callback) {
        loadApplicationObjectsOfClassWithSearch(klass, search, CMRequestOptions.NONE, callback);
    }

    /**
     * Load applications objects of the specific class, filtered by the given search. Recommended that a {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback}
     * is used here, so the returned objects will be accessable without having to be cast
     * @param klass the Class of the objects to load
     * @param search any additional filters, must conform to the CloudMine syntax for searching (ie, "[field = "value"]"
     * @param options any additional options for the query
     * @param callback recommended a {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is passed here
     * @param <CMO> the type of the objects being loaded
     */
    public <CMO extends CMObject> void loadApplicationObjectsOfClassWithSearch(Class<CMO> klass, String search, CMRequestOptions options, Callback<CMObjectResponse> callback) {
        applicationService.asyncLoadObjectsOfClassAndSearch(klass, search, callback, options);
    }

    /**
     * Saves all the application level objects that were added using {@link #addObject(com.cloudmine.api.CMObject)}
     * Note that the object level check occurs on save, not on insertion, so if an object is added and then the object level is
     * modified, it will be saved using the new object level
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation
     */
    public void saveStoreApplicationObjects() throws ConversionException {
        saveStoreApplicationObjects(CMCallback.<ObjectModificationResponse>doNothing());
    }

    /**
     * Saves all the application level objects that were added using {@link #addObject(com.cloudmine.api.CMObject)}
     * Note that the object level check occurs on save, not on insertion, so if an object is added and then the object level is
     * modified, it will be saved using the new object level
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation
     */
    public void saveStoreApplicationObjects(Callback<ObjectModificationResponse> callback) throws ConversionException {
        saveStoreApplicationObjects(callback, CMRequestOptions.NONE);
    }

    /**
     * Saves all the application level objects that were added using {@link #addObject(com.cloudmine.api.CMObject)}
     * Note that the object level check occurs on save, not on insertion, so if an object is added and then the object level is
     * modified, it will be saved using the new object level
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation
     */
    public void saveStoreApplicationObjects(Callback<ObjectModificationResponse> callback, CMRequestOptions options) throws ConversionException {
        applicationService.asyncInsert(getStoreObjectsOfType(ObjectLevel.APPLICATION), callback, options);
    }

    /**
     * Saves all the user level objects that were added using {@link #addObject(com.cloudmine.api.CMObject)}
     * Note that the object level check occurs on save, not on insertion, so if an object is added and then the object level is
     * modified, it will be saved using the new object level
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void saveStoreUserObjects() throws ConversionException, CreationException {
        saveStoreUserObjects(CMCallback.<ObjectModificationResponse>doNothing());
    }

    /**
     * Saves all the user level objects that were added using {@link #addObject(com.cloudmine.api.CMObject)}
     * Note that the object level check occurs on save, not on insertion, so if an object is added and then the object level is
     * modified, it will be saved using the new object level
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void saveStoreUserObjects(Callback<ObjectModificationResponse> callback) throws ConversionException, CreationException {
        saveStoreUserObjects(callback, CMRequestOptions.NONE);
    }

    /**
     * Saves all the user level objects that were added using {@link #addObject(com.cloudmine.api.CMObject)}
     * Note that the object level check occurs on save, not on insertion, so if an object is added and then the object level is
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void saveStoreUserObjects(final Callback<ObjectModificationResponse> callback, final CMRequestOptions options) throws ConversionException, CreationException {
        user().login(new ExceptionPassthroughCallback<LoginResponse>(callback) {
           public void onCompletion(LoginResponse response) {
               userService().asyncInsert(getStoreObjectsOfType(ObjectLevel.USER), callback, options);
           }
        });
    }

    /**
     * Saves all the objects that were added using {@link #addObject(com.cloudmine.api.CMObject)} to their specified level
     * Note that the object level check occurs on save, not on insertion, so if an object is added and then the object level is
     * modified, it will be saved using the new object level
     * Note that this method makes two calls to the CloudMine API; once for application level, once for user level
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it and at least one object to be saved has a {@link ObjectLevel#USER}
     */
    public void saveStoreObjects() throws ConversionException, CreationException {
        saveStoreObjects(CMCallback.<ObjectModificationResponse>doNothing());
    }

    /**
     * Saves all the objects that were added using {@link #addObject(com.cloudmine.api.CMObject)} to their specified level
     * Note that the object level check occurs on save, not on insertion, so if an object is added and then the object level is
     * modified, it will be saved using the new object level
     * Note that this method makes two calls to the CloudMine API; once for application level, once for user level
     * @param appCallback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this. This will get the application level results
     * @param userCallback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this. This will get the user level results
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it and at least one object to be saved has a {@link ObjectLevel#USER}
     */
    public void saveStoreObjects(Callback<ObjectModificationResponse> appCallback, Callback<ObjectModificationResponse> userCallback) throws ConversionException, CreationException {
        saveStoreObjects(appCallback, userCallback, CMRequestOptions.NONE);
    }

    /**
     * Saves all the objects that were added using {@link #addObject(com.cloudmine.api.CMObject)} to their specified level
     * Note that the object level check occurs on save, not on insertion, so if an object is added and then the object level is
     * modified, it will be saved using the new object level
     * Note that this method makes two calls to the CloudMine API; once for application level, once for user level
     * @param appCallback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this. This will get the application level results
     * @param userCallback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this. This will get the user level results
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it and at least one object to be saved has a {@link ObjectLevel#USER}
     */
    public void saveStoreObjects(Callback<ObjectModificationResponse> appCallback, Callback<ObjectModificationResponse> userCallback, CMRequestOptions options) throws ConversionException, CreationException {
        saveStoreUserObjects(userCallback, options);
        saveStoreApplicationObjects(appCallback, options);
    }


    /**
     * Saves all the objects that were added using {@link #addObject(com.cloudmine.api.CMObject)} to their specified level
     * Note that the object level check occurs on save, not on insertion, so if an object is added and then the object level is
     * modified, it will be saved using the new object level
     * Note that this method makes two calls to the CloudMine API; once for application level, once for user level
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this. This will be called twice; once for the application results, once with the user results
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it and at least one object to be saved has a {@link ObjectLevel#USER}
     */
    public void saveStoreObjects(Callback<ObjectModificationResponse> callback) throws ConversionException, CreationException {
        saveStoreObjects(callback, CMRequestOptions.NONE);
    }

    /**
     * Saves all the objects that were added using {@link #addObject(com.cloudmine.api.CMObject)} to their specified level
     * Note that the object level check occurs on save, not on insertion, so if an object is added and then the object level is
     * modified, it will be saved using the new object level
     * Note that this method makes two calls to the CloudMine API; once for application level, once for user level
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this. This will be called twice; once for the application results, once with the user results
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it and at least one object to be saved has a {@link ObjectLevel#USER}
     */
    public void saveStoreObjects(Callback<ObjectModificationResponse> callback, CMRequestOptions options) throws ConversionException, CreationException {
        saveStoreObjects(callback, callback, options);
    }

    private Collection<CMObject> getStoreObjectsOfType(ObjectLevel level) {
        List<CMObject> storeObjects = new ArrayList<CMObject>();
        for(CMObject object : objects.values()) {
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
    public void addObject(CMObject object) {
        objects.put(object.getObjectId(), object);
    }

    /**
     * Add all the given objects to the store. No API calls are performed as a result of this operation
     * @param objects to add to the local store
     */
    public void addObjects(Collection<? extends CMObject> objects) {
        if(objects == null) {
            return;
        }
        for(CMObject object : objects) {
            addObject(object);
        }
    }

    /**
     * Remove the specified object from the store. No API calls are performed as a result of this operation
     * @param object gets removed from the local store
     */
    public void removeObject(CMObject object) {
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
     * Retrieve any existing, added CMObject with the specified objectId
     * @param objectId the objectId associated with the desired CMObject
     * @return the CMObject if it exists; null otherwise
     */
    public CMObject getStoredObject(String objectId) {
        if(objectId == null) {
            return null;
        }
        return objects.get(objectId);
    }

    /**
     * Get all of the objects that have been persisted using this store
     * @return all of the objects that have been persisted using this store
     */
    public List<CMObject> getStoredObjects() {
        return new ArrayList<CMObject>(objects.values());
    }

    /**********************************FILES******************************/

    /**
     * Save the given file asynchronously with its set StoreIdentifier. If StoreIdentifier has not been set, default (application) level is used
     * @param file the file to save
     * @throws CreationException If this CMStore does not have a CMUser associated with it
     */
    public void saveFile(CMFile file) throws CreationException {
        saveFile(file, CMCallback.<FileCreationResponse>doNothing());
    }

    /**
     * Save the given file asynchronously with its set StoreIdentifier. If StoreIdentifier has not been set, default (application) level is used
     * @param file the file to save. Must not be null
     * @param callback will be called on completion; expects a FileCreationResponse, it is recommended a {@link com.cloudmine.api.rest.callbacks.FileCreationResponseCallback} is used here
     * @throws CreationException If this CMStore does not have a CMUser associated with it
     */
    public void saveFile(final CMFile file, final Callback<FileCreationResponse> callback) throws CreationException {
        if(file.isOnLevel(ObjectLevel.USER)) {
            login(user(), new ExceptionPassthroughCallback<LoginResponse>(callback) {
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
     * Retrieve the {@link CMFile} with the specified fileId, if it exists at the application level
     * @param fileId the file fileId, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     */
    public void loadApplicationFile(String fileId) {
        loadApplicationFile(fileId, CMCallback.<FileLoadResponse>doNothing());
    }

    /**
     * Retrieve the {@link CMFile} with the specified fileId, if it exists at the application level
     * @param fileId the file fileId, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects a FileLoadResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.FileLoadCallback} is passed in
     */
    public void loadApplicationFile(String fileId, Callback<FileLoadResponse> callback) {
        loadApplicationFile(fileId, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve the {@link CMFile} with the specified fileId, if it exists at the application level
     * @param fileId the file fileId, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects a FileLoadResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.FileLoadCallback} is passed in
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     */
    public void loadApplicationFile(String fileId, Callback<FileLoadResponse> callback, CMRequestOptions options) {
        applicationService.asyncLoadFile(fileId, callback, options);
    }

    /**
     * Retrieve the {@link CMFile} with the specified fileId, if it exists at the user level
     * @param fileId the file fileId, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void loadUserFile(String fileId) throws CreationException {
        loadUserFile(fileId, CMCallback.<FileLoadResponse>doNothing());
    }

    /**
     * Retrieve the {@link CMFile} with the specified fileId, if it exists at the user level
     * @param fileId the file fileId, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects a FileLoadResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.FileLoadCallback} is passed in
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void loadUserFile(String fileId, Callback<FileLoadResponse> callback) throws CreationException {
        loadUserFile(fileId, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve the {@link CMFile} with the specified fileId, if it exists at the user level
     * @param fileId the file fileId, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects a FileLoadResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.FileLoadCallback} is passed in
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void loadUserFile(final String fileId, final Callback<FileLoadResponse> callback, final CMRequestOptions options) throws CreationException {
        user().login(new ExceptionPassthroughCallback<LoginResponse>(callback) {
            @Override
            public void onCompletion(LoginResponse response) {
                userService().asyncLoadFile(fileId, callback, options);
            }
        });
    }

    /**
     * See {@link #loadApplicationFileMetaData(String, com.cloudmine.api.rest.options.CMRequestOptions, com.cloudmine.api.rest.callbacks.Callback)}
     * @param fileId
     * @param callback
     */
    public void loadApplicationFileMetaData(String fileId, Callback<CMObjectResponse> callback) {
        loadApplicationFileMetaData(fileId, CMRequestOptions.NONE, callback);
    }

    /**
     * Load the meta data for the given fileId
     * @param fileId the fileId of the CMFile whose metadata is to be loaded
     * @param options any post call options
     * @param callback a {@link CMObjectResponse} that will contain the {@link CMFileMetaData} information
     */
    public void loadApplicationFileMetaData(String fileId, CMRequestOptions options, Callback<CMObjectResponse> callback) {
        applicationService.asyncLoadFileMetaData(fileId, options,
                objectLoadUpdateStoreCallback(callback, StoreIdentifier.DEFAULT));
    }

    /**
     * Delete the {@link CMFile} with the specified fileId, if it exists at the application level
     * @param fileId the file fileId, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     */
    public void deleteApplicationFile(String fileId) {
        deleteApplicationFile(fileId, CMCallback.<ObjectModificationResponse>doNothing());
    }

    /**
     * Delete the {@link CMFile} with the specified fileId, if it exists at the application level
     * @param fileId the file fileId, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link ObjectModificationResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in
     */
    public void deleteApplicationFile(String fileId, Callback<ObjectModificationResponse> callback) {
        deleteApplicationFile(fileId, callback, CMRequestOptions.NONE);
    }

    /**
     * Delete the {@link CMFile} with the specified fileId, if it exists at the application level
     * @param fileId the file fileId, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link ObjectModificationResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     */
    public void deleteApplicationFile(String fileId, Callback<ObjectModificationResponse> callback, CMRequestOptions options) {
        applicationService.asyncDeleteFile(fileId, callback, options);
    }

    /**
     * Delete the {@link CMFile} with the specified fileId, if it exists at the user level
     * @param fileId the file fileId, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void deleteUserFile(String fileId) throws CreationException {
        deleteUserFile(fileId, CMCallback.<ObjectModificationResponse>doNothing());
    }

    /**
     * Delete the {@link CMFile} with the specified fileId, if it exists at the user level
     * @param fileId the file fileId, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link ObjectModificationResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void deleteUserFile(String fileId, Callback<ObjectModificationResponse> callback) throws CreationException {
        deleteUserFile(fileId, callback, CMRequestOptions.NONE);
    }

    /**
     * Delete the {@link CMFile} with the specified fileId, if it exists at the user level.
     * @param fileId the file fileId, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link ObjectModificationResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     * @throws CreationException if this CMStore does not have a CMSessionToken associated with it
     */
    public void deleteUserFile(final String fileId, final Callback<ObjectModificationResponse> callback, final CMRequestOptions options) throws CreationException {
        user().login(new ExceptionPassthroughCallback<LoginResponse>(callback) {
            public void onCompletion(LoginResponse response) {
                userService().asyncDeleteFile(fileId, callback, options);
            }
        });
    }

    /**
     * See {@link #loadUserFileMetaData(String, com.cloudmine.api.rest.options.CMRequestOptions, com.cloudmine.api.rest.callbacks.Callback)}
     * @param fileId
     * @param callback
     */
    public void loadUserFileMetaData(final String fileId, final Callback<CMObjectResponse> callback) {
        loadUserFileMetaData(fileId, CMRequestOptions.NONE, callback);
    }

    /**
     * Load {@link CMFileMetaData} about a user file
     * @param fileId
     * @param options
     * @param callback a {@link CMObjectResponseCallback} 
     */
    public void loadUserFileMetaData(final String fileId, final CMRequestOptions options, final Callback<CMObjectResponse> callback) {
        user().login(new ExceptionPassthroughCallback<LoginResponse>(callback) {
            @Override
            public void onCompletion(LoginResponse response) {
                userService().asyncLoadFileMetaData(fileId, options, callback);
            }
        });
    }

    /*********************************USERS*******************************/
    private UserCMWebService userService() throws CreationException {
        try {
            return applicationService.getUserWebService(user().getSessionToken());
        }catch(AccessException ae) {
            throw new CreationException("Cannot get the user service when there is no logged in user associated with this store", ae);
        }
    }

    /**
     * see {@link #loadUserProfilesSearch(String, com.cloudmine.api.rest.callbacks.Callback)}
     * @param searchString
     */
    public void loadUserProfilesSearch(String searchString) {
        loadUserProfilesSearch(searchString, CMCallback.<CMObjectResponse>doNothing());
    }

    /**
     * Search the user profiles for the given string. For more information on the format, see <a href="https://cloudmine.me/docs/object-storage#object_search">the CloudMine documentation on search</a> <br>
     * For example, to search for all users with the field age, where age is > 30, the searchString=[age>30]
     * @param searchString what to search for
     * @param callback will be called after load. Expects a {@link CMObjectResponse}. It is recommended that {@link CMObjectResponseCallback} is used here
     */
    public void loadUserProfilesSearch(String searchString, Callback<CMObjectResponse> callback) {
        loadUserProfilesSearch(searchString, CMRequestOptions.NONE, callback);
    }


    public void loadUserProfilesSearch(String searchString, CMRequestOptions options, Callback<CMObjectResponse> callback) {
        applicationService.asyncSearchUserProfiles(searchString, options, callback);
    }

    /**
     * See {@link #loadAllUserProfiles(com.cloudmine.api.rest.callbacks.Callback)}
     */
    public void loadAllUserProfiles() {
        loadAllUserProfiles(CMCallback.<CMObjectResponse>doNothing());
    }

    /**
     * Load all the user profiles for this application. User profiles include the user id and any profile information,
     * but not the user's e-mail address (unless e-mail address is an additional field added to profile).
     * @param callback A callback that expects a {@link CMObjectResponse}. It is recommended that a {@link CMObjectResponseCallback} is used here
     */
    public void loadAllUserProfiles(Callback<CMObjectResponse> callback) {
        applicationService.asyncLoadAllUserProfiles(callback);
    }

    /**
     * Get the profile for the user associated with this store. If there is no user associated with this store, throws a CreationException
     * @param callback A callback that expects a {@link CMObjectResponse}. It is recommended that a {@link CMObjectResponseCallback} is used here
     */
    public void loadLoggedInUserProfile(Callback<CMObjectResponse> callback) throws CreationException{
        userService().asyncLoadLoggedInUserProfile(callback);
    }

    /**
     * Log in the specified user and set the {@link com.cloudmine.api.JavaCMUser} for this store.
     * @param user the user to log in
     * @return whether the user was set for this store; if false, the user has already been set
     * @throws CreationException if login is called before {@link CMApiCredentials#initialize(String, String)} has been called
     */
    public boolean login(JavaCMUser user) throws CreationException {
        return login(user, CMCallback.<LoginResponse>doNothing());
    }

    /**
     * Log in the specified user and set the {@link com.cloudmine.api.JavaCMUser} for this store
     * @param user the user to log in
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link LoginResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.LoginResponseCallback} is passed in
     * @return whether the user was set for this store; if false, the user has already been set
     * @throws CreationException if login is called before {@link CMApiCredentials#initialize(String, String)} has been called
     */
    public boolean login(JavaCMUser user, Callback<LoginResponse> callback) throws CreationException {
        boolean userSet = setUser(user);
        user.login(callback);
        return userSet;
    }

    /**
     * Sets the user. Can only be called once per store per user; subsequant calls are ignored
     * If you log in via the store, calling this method is unnecessary.
     * @param user a user; if the user is not logged in, they will be before any
     * @return true if the user value was set; false if it has already been set or a null user was given
     */
    public boolean setUser(JavaCMUser user) {
        return this.user.setValue(user);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CMStore cmStore = (CMStore) o;

        if (!applicationService.equals(cmStore.applicationService)) return false;
        if (objects != null ? !objects.equals(cmStore.objects) : cmStore.objects != null) return false;
        if (!user.equals(cmStore.user)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = applicationService.hashCode();
        result = 31 * result + user.hashCode();
        result = 31 * result + (objects != null ? objects.hashCode() : 0);
        return result;
    }
}

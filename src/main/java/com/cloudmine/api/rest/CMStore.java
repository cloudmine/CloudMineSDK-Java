package com.cloudmine.api.rest;

import com.cloudmine.api.*;
import com.cloudmine.api.rest.callbacks.LoginResponseCallback;
import com.cloudmine.api.rest.callbacks.WebServiceCallback;
import com.cloudmine.api.rest.response.LogInResponse;
import com.cloudmine.api.rest.response.ObjectModificationResponse;
import com.cloudmine.api.rest.response.SimpleCMObjectResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 6/14/12, 2:46 PM
 */
public class CMStore {

    private static final Map<StoreIdentifier, CMStore> storeMap = new HashMap<StoreIdentifier, CMStore>();
    static {
        storeMap.put(StoreIdentifier.DEFAULT, new CMStore());
    }
    public static CMStore store() {
        return storeMap.get(StoreIdentifier.DEFAULT);
    }

    public static CMStore store(StoreIdentifier storeId) {
        CMStore store = storeMap.get(storeId);
        if(store == null) {
            store = CMStore.CMStore(storeId);
            storeMap.put(storeId, store);
        }
        return store;
    }

    private final LoginResponseCallback setLoggedInUserCallback(final WebServiceCallback callback) {
        return new LoginResponseCallback() {
            public void onCompletion(LogInResponse response) {
                if(response.wasSuccess()) {
                    setLoggedInUser(response.userToken());
                }
                callback.onCompletion(response);
            }
        };
    }

    private final CMWebService applicationService;
    private final Immutable<CMUserToken> loggedInUserToken = new Immutable<CMUserToken>();


    public static CMStore CMStore(StoreIdentifier identifier) {
        return new CMStore(identifier);
    }

    public static CMStore CMStore() {
        return CMStore(StoreIdentifier.DEFAULT);
    }

    private CMStore() {
        this(StoreIdentifier.DEFAULT);
    }

    private CMStore(StoreIdentifier identifier) {
        if(identifier.isUserLevel()) {
            setLoggedInUser(identifier.userToken());
        }
        applicationService = CMWebService.service();
    }

    private CMUserToken loggedInUserToken() {
        return loggedInUserToken.value(CMUserToken.FAILED);
    }

    /**
     * Asynchronously save the object based on the StoreIdentifier associated with it. If no StoreIdentifier is
     * present, default (app level) is used.
     * @param object
     * @return
     */
    public Future<ObjectModificationResponse> saveObject(SimpleCMObject object) {
        return saveObject(object, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> saveObject(SimpleCMObject object, WebServiceCallback callback) {
        return serviceForObject(object).asyncInsert(object, callback);
    }

    public Future<SimpleCMObjectResponse> allObjects(WebServiceCallback callback) {
        return applicationService.asyncLoadObjects(callback);
    }

    public Future<SimpleCMObjectResponse> allUserObjects(WebServiceCallback callback) {
        return applicationService.userWebService(loggedInUserToken()).asyncLoadObjects(callback);
    }

    public Future<LogInResponse> login(CMUser user) {
        return login(user, WebServiceCallback.DO_NOTHING);
    }

    public Future<LogInResponse> login(CMUser user, WebServiceCallback callback) {
        return applicationService.asyncLogin(user, setLoggedInUserCallback(callback));
    }

    private CMWebService serviceForObject(SimpleCMObject object) {
        switch(object.savedWith().level()) {
            case USER:
                return applicationService.userWebService(loggedInUserToken());
            case UNKNOWN:
            case APPLICATION:
            default:
                return applicationService;
        }
    }

    /**
     * Sets the logged in user. Can only be called once per store per user; subsequant calls are ignored
     * as long as the passed in token was not from a failed log in. If you log in via the store, calling
     * this method is unnecessary.
     * @param token received from a LoginResponse
     * @return true if the logged in user value was set; false if it has already been set or a failed log in token was given
     */
    public boolean setLoggedInUser(CMUserToken token) {
        if(CMUserToken.FAILED.equals(token)) {
            return false;
        }
        return loggedInUserToken.setValue(token);
    }

}

package com.cloudmine.api.rest;

import com.cloudmine.api.CMUserToken;
import com.cloudmine.api.SimpleCMObject;
import com.cloudmine.api.StoreIdentifier;
import com.cloudmine.api.rest.callbacks.WebServiceCallback;
import com.cloudmine.api.rest.response.ObjectModificationResponse;

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

    private final CMWebService applicationService;
    private final CMUserToken loggedInUserToken;


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
            loggedInUserToken = identifier.userToken();
            applicationService = CMWebService.service().userWebService(loggedInUserToken);
        } else {
            //default to application level
            applicationService = CMWebService.service();
            loggedInUserToken = CMUserToken.FAILED;
        }
    }

    /**
     * Save the object whatever
     * @param object
     * @return
     */
    public Future<ObjectModificationResponse> saveObject(SimpleCMObject object) {
        return saveObject(object, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> saveObject(SimpleCMObject object, WebServiceCallback callback) {
        return applicationService.asyncInsert(object, callback);
    }


}

package com.cloudmine.api.rest;

import com.cloudmine.api.*;
import com.cloudmine.api.rest.callbacks.LoginResponseCallback;
import com.cloudmine.api.rest.callbacks.WebServiceCallback;
import com.cloudmine.api.rest.response.LogInResponse;
import com.cloudmine.api.rest.response.ObjectModificationResponse;
import com.cloudmine.api.rest.response.SimpleCMObjectResponse;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 6/14/12, 2:46 PM
 */
public class CMStore {

    private static final Map<StoreIdentifier, CMStore> storeMap = new HashMap<StoreIdentifier, CMStore>();
    public static final String EMPTY_SUCCESS_RESPONSE = "{\n" +
            "                            \"success\":{}\n" +
            "                        }";

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
    private final Map<String, SimpleCMObject> applicationObjects = new HashMap<String, SimpleCMObject>();
    private final Map<String, SimpleCMObject> userObjects = new HashMap<String, SimpleCMObject>();
    private final Map<String, SimpleCMObject> objects = new ConcurrentHashMap<String, SimpleCMObject>();
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

    /*****************************OBJECTS********************************/

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
        return saveObject(object, callback, CMRequestOptions.NONE);
    }

    public Future<ObjectModificationResponse> saveObject(SimpleCMObject object, WebServiceCallback callback, CMRequestOptions options) {
        return serviceForObject(object).asyncInsert(object, callback, options);
    }

    public Future<ObjectModificationResponse> deleteObject(SimpleCMObject object) {
        return deleteObject(object, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> deleteObject(SimpleCMObject object, WebServiceCallback callback) {
        return deleteObject(object, callback, CMRequestOptions.NONE);
    }

    public Future<ObjectModificationResponse> deleteObject(SimpleCMObject object, WebServiceCallback callback, CMRequestOptions options) {
        return serviceForObject(object).asyncDeleteObject(object, callback, options);
    }

    public Future<SimpleCMObjectResponse> allApplicationObjects(WebServiceCallback callback) {
        return allApplicationObjects(callback, CMRequestOptions.NONE);
    }

    public Future<SimpleCMObjectResponse> allApplicationObjects(WebServiceCallback callback, CMRequestOptions options) {
        return applicationService.asyncLoadObjects(callback, options);
    }

    public Future<SimpleCMObjectResponse> allUserObjects(WebServiceCallback callback) {
        return allUserObjects(callback, CMRequestOptions.NONE);
    }
    
    public Future<SimpleCMObjectResponse> allUserObjects(WebServiceCallback callback, CMRequestOptions options) {
        return userService().asyncLoadObjects(callback, options);
    }

    public Future<SimpleCMObjectResponse> applicationObjectsWithKeys(Collection<String> keys) {
        return applicationObjectsWithKeys(keys, WebServiceCallback.DO_NOTHING);
    }

    public Future<SimpleCMObjectResponse> applicationObjectsWithKeys(Collection<String> keys, WebServiceCallback callback) {
        return applicationObjectsWithKeys(keys, callback, CMRequestOptions.NONE);
    }
    
    public Future<SimpleCMObjectResponse> applicationObjectsWithKeys(Collection<String> keys, WebServiceCallback callback, CMRequestOptions options) {
        return applicationService.asyncLoadObjects(keys, callback, options);
    }

    public Future<SimpleCMObjectResponse> userObjectsWithKeys(Collection<String> keys) {
        return userObjectsWithKeys(keys, WebServiceCallback.DO_NOTHING);
    }

    public Future<SimpleCMObjectResponse> userObjectsWithKeys(Collection<String> keys, WebServiceCallback callback) {
        return userObjectsWithKeys(keys, callback, CMRequestOptions.NONE);
    }

    public Future<SimpleCMObjectResponse> userObjectsWithKeys(Collection<String> keys, WebServiceCallback callback, CMRequestOptions options) {
        return userService().asyncLoadObjects(keys, callback, options);
    }

    public Future<SimpleCMObjectResponse> userObjectsSearch(String search) {
        return userObjectsSearch(search, WebServiceCallback.DO_NOTHING);
    }

    public Future<SimpleCMObjectResponse> userObjectsSearch(String search, WebServiceCallback callback) {
        return userObjectsSearch(search, callback, CMRequestOptions.NONE);
    }

    public Future<SimpleCMObjectResponse> userObjectsSearch(String search, WebServiceCallback callback, CMRequestOptions options) {
        return userService().asyncSearch(search, callback, options);
    }

    public Future<SimpleCMObjectResponse> applicationObjectsSearch(String search) {
        return applicationObjectsSearch(search, WebServiceCallback.DO_NOTHING);
    }

    public Future<SimpleCMObjectResponse> applicationObjectsSearch(String search, WebServiceCallback callback) {
        return applicationObjectsSearch(search, callback, CMRequestOptions.NONE);
    }

    public Future<SimpleCMObjectResponse> applicationObjectsSearch(String search, WebServiceCallback callback, CMRequestOptions options) {
        return applicationService.asyncSearch(search, callback, options);
    }

    public Future<SimpleCMObjectResponse> userObjectsOfClass(String klass) {
        return userObjectsOfClass(klass, WebServiceCallback.DO_NOTHING);
    }

    public Future<SimpleCMObjectResponse> userObjectsOfClass(String klass, WebServiceCallback callback) {
        return userObjectsOfClass(klass, callback, CMRequestOptions.NONE);
    }

    public Future<SimpleCMObjectResponse> userObjectsOfClass(String klass, WebServiceCallback callback, CMRequestOptions options) {
        return userService().asyncLoadObjectsOfClass(klass, callback, options);
    }

    public Future<ObjectModificationResponse> saveStoreApplicationObjects() {
        return saveStoreApplicationObjects(WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> saveStoreApplicationObjects(WebServiceCallback callback) {
        return saveStoreApplicationObjects(callback, CMRequestOptions.NONE);
    }

    public Future<ObjectModificationResponse> saveStoreApplicationObjects(WebServiceCallback callback, CMRequestOptions options) {
        return applicationService.asyncInsert(getStoreObjectsOfType(ObjectLevel.APPLICATION), callback, options);
    }

    public Future<ObjectModificationResponse> saveStoreUserObjects() {
        return saveStoreUserObjects(WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> saveStoreUserObjects(WebServiceCallback callback) {
        return saveStoreUserObjects(callback, CMRequestOptions.NONE);
    }

    public Future<ObjectModificationResponse> saveStoreUserObjects(WebServiceCallback callback, CMRequestOptions options) {
        return userService().asyncInsert(getStoreObjectsOfType(ObjectLevel.USER), callback, options);
    }

    public void saveStoreObjects() {
        saveStoreObjects(WebServiceCallback.DO_NOTHING);
    }

    public void saveStoreObjects(WebServiceCallback appCallback, WebServiceCallback userCallback) {
        saveStoreObjects(appCallback, userCallback, CMRequestOptions.NONE);
    }

    public void saveStoreObjects(WebServiceCallback appCallback, WebServiceCallback userCallback, CMRequestOptions options) {
        saveStoreUserObjects(userCallback, options);
        saveStoreApplicationObjects(appCallback, options);
    }

    /**
     * Save all the objects in the store
     * @param callback This callback will be called twice; once for user objects being stored, and once for application objects being stored
     */
    public void saveStoreObjects(WebServiceCallback callback) {
        saveStoreObjects(callback, CMRequestOptions.NONE);
    }
    
    public void saveStoreObjects(WebServiceCallback callback, CMRequestOptions options) {
        saveStoreObjects(callback, callback, options);
    }

//    public void saveStoreObjects(final WebServiceCallback callback) {
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
                storeObjects.add(object);
            }
        }
        return storeObjects;
    }

    public void addObject(SimpleCMObject object) {
        objects.put(object.key(), object);
    }

    public void removeObject(SimpleCMObject object) {
        objects.remove(object.key());
    }

    public SimpleCMObject getStoredObject(String key) {
        return objects.get(key);
    }

    /**********************************FILES******************************/

    public Future<CMFile> applicationFile(String name) {
        return applicationFile(name, WebServiceCallback.DO_NOTHING);
    }

    public Future<CMFile> applicationFile(String name, WebServiceCallback callback) {
        return applicationFile(name, callback, CMRequestOptions.NONE);
    }

    public Future<CMFile> applicationFile(String name, WebServiceCallback callback, CMRequestOptions options) {
        return applicationService.asyncLoadFile(name, callback, options);
    }

    public Future<CMFile> userFile(String name) {
        return userFile(name, WebServiceCallback.DO_NOTHING);
    }

    public Future<CMFile> userFile(String name, WebServiceCallback callback) {
        return userFile(name, callback, CMRequestOptions.NONE);
    }

    public Future<CMFile> userFile(String name, WebServiceCallback callback, CMRequestOptions options) {
        return userService().asyncLoadFile(name, callback, options);
    }

    public Future<ObjectModificationResponse> deleteApplicationFile(String fileName) {
        return deleteApplicationFile(fileName, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> deleteApplicationFile(String name, WebServiceCallback callback) {
        return deleteApplicationFile(name, callback, CMRequestOptions.NONE);
    }

    public Future<ObjectModificationResponse> deleteApplicationFile(String name, WebServiceCallback callback, CMRequestOptions options) {
        return applicationService.asyncDeleteFile(name, callback, options);
    }

    public Future<ObjectModificationResponse> deleteUserFile(String fileName) {
        return deleteUserFile(fileName, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> deleteUserFile(String fileName, WebServiceCallback callback) {
        return deleteUserFile(fileName, callback, CMRequestOptions.NONE);
    }

    public Future<ObjectModificationResponse> deleteUserFile(String fileName, WebServiceCallback callback, CMRequestOptions options) {
        return userService().asyncDeleteFile(fileName, callback, options);
    }

    /*********************************USERS*******************************/


    private UserCMWebService userService() {
        return applicationService.userWebService(loggedInUserToken());
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
                return userService();
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

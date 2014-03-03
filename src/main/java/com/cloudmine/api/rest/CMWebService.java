package com.cloudmine.api.rest;

import com.cloudmine.api.CMApiCredentials;
import com.cloudmine.api.CMChannel;
import com.cloudmine.api.CMFile;
import com.cloudmine.api.CMObject;
import com.cloudmine.api.CMPushNotification;
import com.cloudmine.api.CMSessionToken;
import com.cloudmine.api.JavaCMUser;
import com.cloudmine.api.JavaCMUser;
import com.cloudmine.api.LibrarySpecificClassCreator;
import com.cloudmine.api.Strings;
import com.cloudmine.api.exceptions.ConversionException;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.exceptions.NetworkException;
import com.cloudmine.api.persistance.ClassNameRegistry;
import com.cloudmine.api.rest.callbacks.CMCallback;
import com.cloudmine.api.rest.callbacks.CMResponseCallback;
import com.cloudmine.api.rest.callbacks.Callback;
import com.cloudmine.api.rest.options.CMRequestOptions;
import com.cloudmine.api.rest.response.CMObjectResponse;
import com.cloudmine.api.rest.response.CMResponse;
import com.cloudmine.api.rest.response.CMSocialLoginResponse;
import com.cloudmine.api.rest.response.CreationResponse;
import com.cloudmine.api.rest.response.FileCreationResponse;
import com.cloudmine.api.rest.response.FileLoadResponse;
import com.cloudmine.api.rest.response.ListOfValuesResponse;
import com.cloudmine.api.rest.response.LoginResponse;
import com.cloudmine.api.rest.response.ObjectModificationResponse;
import com.cloudmine.api.rest.response.PushChannelResponse;
import com.cloudmine.api.rest.response.ResponseBase;
import com.cloudmine.api.rest.response.ResponseConstructor;
import com.cloudmine.api.rest.response.TokenUpdateResponse;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Provides direct access to the CloudMine API. Useful if you don't need all the bookkeeping of a {@link CMStore}. Also
 * provides some synchronous implementations of API calls, for when you know you are not on the UI thread.
 * This base class performs all operations at the Application level. To perform operations at the User level, use a
 * {@link UserCMWebService}<br>
 * Preconditions for use:<br>
 * {@link com.cloudmine.api.BaseDeviceIdentifier#initialize(android.content.Context)} has been called with the activity context, if developing on Android<br>
 * {@link CMApiCredentials#initialize(String, String)} has been called with the application identifier and API key<br>

 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class CMWebService {
    private static CMWebService service = new CMWebService(CMApiCredentials.getApplicationIdentifier(), LibrarySpecificClassCreator.getCreator().getAsynchronousHttpClient());
    private static final Logger LOG = LoggerFactory.getLogger(CMWebService.class);
    public static final Header JSON_HEADER = new BasicHeader("Content-Type", "application/json");

    public static final String PASSWORD_KEY = "password";
    public static final String JSON_ENCODING = "UTF-8";
    public static final String AUTHORIZATION_KEY = "Authorization";

    public static final String EMAIL_KEY = "email";


    protected final CMURLBuilder baseUrl;
    private final HttpClient httpClient = new DefaultHttpClient();
    protected final AsynchronousHttpClient asyncHttpClient; //TODO split this into an asynch and synch impl instead of both in one?
    private CMSessionToken loggedInSessionToken;
    private final Map<CMSessionToken, UserCMWebService> loggedInUserServices = new WeakHashMap<CMSessionToken, UserCMWebService>();

    /**
     * Get the instance of CMWebService. You should use this method instead of constructing your own,
     * as the actual implementation of CMWebService differs based on what platform your code is running
     * on.
     * @return a platform appropriate implementation of CMWebService
     * @throws CreationException if CMApiCredentials.initialize has not yet been called
     */
    public static CMWebService getService() throws CreationException {
        return service;
    }

    public static CMWebService getService(String appId, String apiKey) {
        CMApiCredentials.initialize(appId, apiKey);
        service = new CMWebService(CMApiCredentials.getApplicationIdentifier(), LibrarySpecificClassCreator.getCreator().getAsynchronousHttpClient());
        return service;
    }

    protected CMWebService(CMURLBuilder baseUrl, AsynchronousHttpClient asyncClient) {
        this.baseUrl = baseUrl;
        asyncHttpClient = asyncClient;
    }

    protected CMWebService(String appId, AsynchronousHttpClient asyncClient) {
        this(new CMURLBuilder(appId), asyncClient);
    }

    /**
     * If the entity response is not fully consumed, the connection will not be released, so once you're
     * done with an HttpResponse you need to consume the rest of the content
     * @param response the response to consume
     */
    public static void consumeEntityResponse(HttpResponse response) {
        if(response != null && response.getEntity() != null) {
            HttpEntity body = response.getEntity();
            if(body.isStreaming()) {
                try {
                    InputStream instream = body.getContent();
                    if(instream != null) {
                        instream.close();
                    }
                } catch (IOException e) {
                    //GNF
                }
            }
        }
    }

    /**
     * Get a UserCMWebService for the given token; if none already exist, one will be created. This lets
     * you perform operations at the user level, such as object persistance
     * @param token a session token return from a valid login request or a logged in user
     * @return A UserCMWebService that includes the given session token in its requests and operates at the user level
     * @throws CreationException if given a null or failed session token
     */
    public UserCMWebService getUserWebService(CMSessionToken token) throws CreationException {
        return createUserCMWebService(token);
    }

    protected UserCMWebService createUserCMWebService(CMSessionToken token) throws CreationException {
        if(token == null || CMSessionToken.FAILED.equals(token)) {
            throw new CreationException("Cannot create a UserCMWebService off a failed or null token");
        }
        UserCMWebService userService = loggedInUserServices.get(token);
        if(userService == null) {
            userService = UserCMWebService.UserCMWebService(baseUrl.copy().user(), token, asyncHttpClient);
            loggedInUserServices.put(token, userService);
        }
        return userService;
    }



    /**
     * This will set the default UserCMWebService and return it. This must be called before calling
     * userWebService, unless you pass userWebService a CMSessionToken.
     * @param token the token retrieved from a LoginResponse
     * @return the UserCMWebService that is created from this request.
     */
    public synchronized UserCMWebService setLoggedInUser(CMSessionToken token) throws CreationException {
        loggedInSessionToken = token;
        return getUserWebService(token);
    }

    /**
     * Get the 'default' user web service. Requires that setUser has been called
     * @return the UserCMWebService for the logged in user
     * @throws CreationException if setUser has not been called, or was called with an invalid value
     */
    public synchronized UserCMWebService getUserWebService() throws CreationException {
        if(loggedInSessionToken == null) {
            throw new CreationException("Cannot request a user web service until setUser has been called");
        }
        return getUserWebService(loggedInSessionToken);
    }

    /**
     * Delete all objects. Be careful...
     * @return the ObjectModificationResponse containing information about what was deleted
     * @throws NetworkException if unable to perform the request
     */
    public ObjectModificationResponse deleteAll() throws NetworkException {
        return executeCommand(createDeleteAll(), objectModificationResponseConstructor());
    }

    /**
     * Delete the specified objects, based on their object id
     * @param objectIds the object ids of the objects to delete
     * @return the ObjectModificationResponse containing information about what was deleted
     * @throws NetworkException if unable to perform the request
     */
    public ObjectModificationResponse delete(Collection<String> objectIds) throws NetworkException {
        return executeCommand(createDelete(objectIds), objectModificationResponseConstructor());
    }

    /**
     * Delete the specified object, based on its object id
     * @param objectId the object id of the object to delete
     * @return the ObjectModificationResponse containing information about what was deleted
     * @throws NetworkException if unable to perform the request
     */
    public ObjectModificationResponse delete(String objectId) throws NetworkException {
        return executeCommand(createDelete(objectId), objectModificationResponseConstructor());
    }

    /**
     * Load all of the objects of the specified class
     * @param klass the class of the objects to load; this is either inferred directly or you can override {@link com.cloudmine.api.CMObject#getClassName}
     */
    public void asyncLoadObjectsOfClass(String klass) {
        asyncLoadObjectsOfClass(klass, CMCallback.<CMObjectResponse>doNothing());
    }

    /**
     * Load all of the objects of the specified class
     * @param klass the class of the objects to load; this is either inferred directly or you can override {@link com.cloudmine.api.CMObject#getClassName}
     * @param callback
     */
    public void asyncLoadObjectsOfClass(String klass, Callback<CMObjectResponse> callback) {
        asyncLoadObjectsOfClass(klass, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the objects that are of the specified class. Class values are either inferred directly or you can override {@link com.cloudmine.api.CMObject#getClassName}
     * @param klass the class type to load
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used here
     */
    public void asyncLoadObjectsOfClass(String klass, Callback<CMObjectResponse> callback, CMRequestOptions options) {
        HttpGet search = createSearch("[" + getClassSearchString(klass) + "]", options);
        executeAsyncCommand(search, callback, cmObjectResponseConstructor());

    }

    public void asyncLoadObjectsOfClass(Class<? extends CMObject> klass, Callback<CMObjectResponse> callback) {
        asyncLoadObjectsOfClass(klass, callback, CMRequestOptions.NONE);
    }

    public void asyncLoadObjectsOfClass(Class<? extends CMObject> klass, Callback<CMObjectResponse> callback, CMRequestOptions options) {
        HttpGet search = createSearch("[" + getClassSearchString(klass) + "]", options);
        executeAsyncCommand(search,
                callback, cmObjectResponseConstructor());
    }

    public void asyncLoadObjectsOfClassAndSearch(Class<? extends CMObject> klass, String search, Callback<CMObjectResponse> callback) {
        asyncLoadObjectsOfClassAndSearch(klass, search, callback, CMRequestOptions.NONE);
    }

    public void asyncLoadObjectsOfClassAndSearch(Class<? extends CMObject> klass, String search, Callback<CMObjectResponse> callback, CMRequestOptions options) {
        executeAsyncCommand(createSearch(addClassSearch(klass, search), options),
                callback, cmObjectResponseConstructor());

    }

    private String getClassSearchString(Class<? extends CMObject> klass) {
        String className = ClassNameRegistry.forClass(klass);
        return getClassSearchString(className);
    }

    private String getClassSearchString(String className) {
        return JsonUtilities.CLASS_KEY + "=" + JsonUtilities.addQuotes(className);
    }

    private String addClassSearch(Class<? extends CMObject> klass, String search) {
        int endOfSearch = search.lastIndexOf("]");
        if(endOfSearch == -1) {
            return search; //this is an invalid search which will fail server side
        }
        String openSearch = search.substring(0, endOfSearch);
        openSearch += ", " + getClassSearchString(klass) + "]";
        return openSearch;
    }

    public void asyncLoadAllUserProfiles(Callback<CMObjectResponse> callback) {
        executeAsyncCommand(createGetAllUsers(), callback, cmObjectResponseConstructor());
    }

    public void asyncSearchUserProfiles(String searchString, Callback<CMObjectResponse> callback) {
        asyncSearchUserProfiles(searchString, CMRequestOptions.NONE, callback);
    }

    public void asyncSearchUserProfiles(String searchString, CMRequestOptions options, Callback<CMObjectResponse> callback) {
        executeAsyncCommand(createProfileSearch(searchString, options), callback, cmObjectResponseConstructor());
    }


    /**
     * Delete the given object from CloudMine.
     * @param object to delete; this is done based on the object id, its values are ignored
     */
    public void asyncDeleteObject(CMObject object) {
        asyncDeleteObject(object, CMCallback.<ObjectModificationResponse>doNothing());
    }

    /**
     * Delete the given object from CloudMine.
     * @param object to delete; this is done based on the object id, its values are ignored
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     */
    public void asyncDeleteObject(CMObject object, Callback<ObjectModificationResponse> callback) {
        asyncDeleteObject(object, callback, CMRequestOptions.NONE);
    }

    /**
     * Delete the given object from CloudMine.
     * @param object to delete; this is done based on the object id, its values are ignored
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     */
    public void asyncDeleteObject(CMObject object, Callback<ObjectModificationResponse> callback, CMRequestOptions options) {
        asyncDeleteObjects(Collections.singletonList(object), callback, options);
    }

    /**
     * Delete the given objects from CloudMine.
     * @param objects to delete; this is done based on the object ids, values are ignored
     */
    public void asyncDeleteObjects(Collection<? extends CMObject> objects) {
        asyncDeleteObjects(objects, CMCallback.<ObjectModificationResponse>doNothing());
    }

    /**
     * Delete the given objects from CloudMine.
     * @param objects to delete; this is done based on the object ids, values are ignored
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     */
    public void asyncDeleteObjects(Collection<? extends CMObject> objects, Callback<ObjectModificationResponse> callback) {
        asyncDeleteObjects(objects, callback, CMRequestOptions.NONE);
    }

    /**
     * Delete the given objects from CloudMine.
     * @param objects to delete; this is done based on the object ids, values are ignored
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     */
    public void asyncDeleteObjects(Collection<? extends CMObject> objects, Callback<ObjectModificationResponse> callback, CMRequestOptions options) {
        int size = objects.size();
        Collection<String> keys = new ArrayList<String>(size);
        for(CMObject object : objects) {
            keys.add(object.getObjectId());
        }
        asyncDelete(keys, callback, options);
    }

    /**
     * Delete the given object from CloudMine.
     * @param objectId to delete; this is done based on the object id
     */
    public void asyncDelete(String objectId) {
        asyncDelete(objectId, CMCallback.<ObjectModificationResponse>doNothing());
    }

    /**
     * Delete the given object from CloudMine.
     * @param objectId to delete; this is done based on the object id
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     */
    public void asyncDelete(String objectId, Callback<ObjectModificationResponse> callback) {
        asyncDelete(objectId, callback, CMRequestOptions.NONE);
    }

    /**
     * Delete the given object from CloudMine.
     * @param objectId to delete; this is done based on the object id
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     */
    public void asyncDelete(String objectId, Callback<ObjectModificationResponse> callback, CMRequestOptions options) {
        asyncDelete(Collections.singletonList(objectId), callback, options);
    }

    /**
     * Delete the given objects from CloudMine.
     * @param objectIds to delete; this is done based on the object ids
     */
    public void asyncDelete(Collection<String> objectIds) {
        asyncDelete(objectIds, CMCallback.<ObjectModificationResponse>doNothing());
    }

    /**
     * Delete the given objects from CloudMine.
     * @param objectIds to delete; this is done based on the object ids
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     */
    public void asyncDelete(Collection<String> objectIds, Callback<ObjectModificationResponse> callback) {
        asyncDelete(objectIds, callback, CMRequestOptions.NONE);
    }

    /**
     * Delete the given objects from CloudMine.
     * @param objectIds to delete; this is done based on the object ids
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     */
    public void asyncDelete(Collection<String> objectIds, Callback<ObjectModificationResponse> callback, CMRequestOptions options) {
        executeAsyncCommand(createDelete(objectIds, options),
                callback, objectModificationResponseConstructor());
    }

    /**
     * This will delete ALL the objects associated with this API key. Be careful...
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     */
    public void asyncDeleteAll(Callback<ObjectModificationResponse> callback) {
        executeAsyncCommand(createDeleteAll(), callback, objectModificationResponseConstructor());
    }

    /**
     * This will delete ALL the objects associated with this API key. Be careful...
     */
    public void asyncDeleteAll() {
        asyncDeleteAll(CMCallback.<ObjectModificationResponse>doNothing());
    }

    public void asyncDeleteUser(String userId) {
        asyncDeleteUser(userId, CMCallback.<ObjectModificationResponse>doNothing());
    }

    public void asyncDeleteUser(String userId, Callback<ObjectModificationResponse> callback) {
        executeAsyncCommand(createDeleteUser(userId), callback, objectModificationResponseConstructor());
    }

    /**
     * Delete the CMFile
     * @param file the file to delete
     */
    public void asyncDeleteFile(CMFile file) {
        asyncDeleteFile(file, CMCallback.<ObjectModificationResponse>doNothing());
    }
    /**
     * Delete the CMFile
     * @param file the file to delete
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link ObjectModificationResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in
     */
    public void asyncDeleteFile(CMFile file, Callback<ObjectModificationResponse> callback) {
        asyncDelete(file.getFileId(), callback);
    }

    /**
     * Delete all the given CMFiles
     * @param files the files to delete
     */
    public void asyncDeleteFiles(Collection<CMFile> files) {
        asyncDeleteFiles(files, CMCallback.<ObjectModificationResponse>doNothing());
    }

    /**
     * Delete all the given CMFiles
     * @param files the files to delete
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link ObjectModificationResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in
     */
    public void asyncDeleteFiles(Collection<CMFile> files, Callback<ObjectModificationResponse> callback) {
        Collection<String> keys = new ArrayList<String>(files.size());
        for(CMFile file : files) {
            keys.add(file.getFileId());
        }
        asyncDelete(keys, callback);
    }

    /**
     * Delete the {@link CMFile} with the specified fileId
     * @param fileId the file fileId, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     */
    public void asyncDeleteFile(String fileId) {
        asyncDeleteFile(fileId, CMCallback.<ObjectModificationResponse>doNothing());
    }

    /**
     * Delete the {@link CMFile} with the specified fileId
     * @param fileId the file fileId, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link ObjectModificationResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in
     */
    public void asyncDeleteFile(String fileId, Callback<ObjectModificationResponse> callback) {
        asyncDeleteFile(fileId, callback, CMRequestOptions.NONE);
    }

    /**
     * Delete the {@link CMFile} with the specified fileId
     * @param fileId the file fileId, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link ObjectModificationResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     */
    public void asyncDeleteFile(String fileId, Callback<ObjectModificationResponse> callback, CMRequestOptions options) {
        asyncDelete(fileId, callback, options);
    }

    /**
     * Add the given file to CloudMine
     * @param file the file to add
     */
    public void asyncUpload(CMFile file) {
        asyncUpload(file, CMCallback.<FileCreationResponse>doNothing());
    }

    /**
     * Add the given file to CloudMine
     * @param file the file to add
     * @param callback a {@link Callback} that expects a {@link FileCreationResponse}. It is recommended that you pass in a {@link com.cloudmine.api.rest.callbacks.FileCreationResponseCallback}
     */
    public void asyncUpload(CMFile file, Callback<FileCreationResponse> callback) {
        executeAsyncCommand(createPut(file), callback, fileCreationResponseConstructor());
    }

    /**
     * Retrieve the {@link CMFile} with the specified fileId, if it exists
     * @param fileId the file fileId, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     */
    public void asyncLoadFile(String fileId) {
        asyncLoadFile(fileId, CMCallback.<FileLoadResponse>doNothing());
    }

    /**
     * Retrieve the {@link CMFile} with the specified fileId, if it exists
     * @param fileId the file fileId, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects a FileLoadResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.FileLoadCallback} is passed in
     */
    public void asyncLoadFile(String fileId, Callback<FileLoadResponse> callback) {
        asyncLoadFile(fileId, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve the {@link CMFile} with the specified fileId, if it exists
     * @param fileId the file fileId, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects a FileLoadResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.FileLoadCallback} is passed in
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     */
    public void asyncLoadFile(String fileId, Callback<FileLoadResponse> callback, CMRequestOptions options) {
        executeAsyncCommand(createGetFile(fileId, options),
                callback, fileLoadResponseResponseConstructor(fileId));
    }

    public void asyncLoadFileMetaData(String fileId, CMRequestOptions options, Callback<CMObjectResponse> callback) {
        executeAsyncCommand(createGetFileMetaData(fileId, options), callback, cmObjectResponseConstructor());
    }

    /**
     * Retrieve all the objects
     */
    public void asyncLoadObjects() {
        asyncLoadObjects(CMCallback.<CMObjectResponse>doNothing());
    }

    /**
     * Retrieve all the objects
     * @param callback a Callback that expects a {@link com.cloudmine.api.rest.response.CMObjectResponse}. It is recommended that a {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used
     */
    public void asyncLoadObjects(Callback<CMObjectResponse> callback) {
        asyncLoadObjects(callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the objects
     * @param callback a Callback that expects a {@link com.cloudmine.api.rest.response.CMObjectResponse}. It is recommended that a {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     */
    public void asyncLoadObjects(Callback<CMObjectResponse> callback, CMRequestOptions options) {
        asyncLoadObjects(Collections.<String>emptyList(), callback, options);
    }

    /**
     * Retrieve the object with the given objectId
     * @param objectId the top level objectId of the object to retrieve
     */
    public void asyncLoadObject(String objectId) {
        asyncLoadObject(objectId, CMCallback.<CMObjectResponse>doNothing());
    }

    /**
     * Retrieve the object with the given objectId
     * @param objectId the top level objectId of the object to retrieve
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used here
     */
    public void asyncLoadObject(String objectId, Callback<CMObjectResponse> callback) {
        asyncLoadObjects(Collections.<String>singleton(objectId), callback);
    }

    /**
     * Retrieve the object with the given objectId
     * @param objectId the top level objectId of the object to retrieve
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used here
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     */
    public void asyncLoadObject(String objectId, Callback<CMObjectResponse> callback, CMRequestOptions options) {
        asyncLoadObjects(Collections.<String>singleton(objectId), callback, options);
    }

    /**
     * Retrieve all the objects with the given objectIds
     * @param objectIds the top level objectIds of the objects to retrieve
     */
    public void asyncLoadObjects(Collection<String> objectIds) {
        asyncLoadObjects(objectIds, CMCallback.<CMObjectResponse>doNothing());
    }

    /**
     * Retrieve all the objects with the given objectIds
     * @param objectIds the top level objectIds of the objects to retrieve
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used here
     */
    public void asyncLoadObjects(Collection<String> objectIds, Callback<CMObjectResponse> callback) {
        asyncLoadObjects(objectIds, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the objects with the given objectIds
     * @param objectIds the top level objectIds of the objects to retrieve
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used here
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     */
    public void asyncLoadObjects(Collection<String> objectIds, Callback<CMObjectResponse> callback, CMRequestOptions options) {
        executeAsyncCommand(createGetObjects(objectIds, options),
                callback, cmObjectResponseConstructor());
    }

    /**
     * Retrieve all the objects that match the given search
     * @param searchString the search string to use. For more information on syntax. See <a href="https://cloudmine.me/docs/object-storage#query_syntax">Search query syntax</a>
     */
    public void asyncSearch(String searchString) {
        asyncSearch(searchString, CMCallback.<CMObjectResponse>doNothing());
    }

    /**
     * Retrieve all the objects that match the given search
     * @param searchString the search string to use. For more information on syntax. See <a href="https://cloudmine.me/docs/object-storage#query_syntax">Search query syntax</a>
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used here
     */
    public void asyncSearch(String searchString, Callback<CMObjectResponse> callback) {
        asyncSearch(searchString, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the objects that match the given search
     * @param searchString the search string to use. For more information on syntax. See <a href="https://cloudmine.me/docs/object-storage#query_syntax">Search query syntax</a>
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used here
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     */
    public void asyncSearch(String searchString, Callback<CMObjectResponse> callback, CMRequestOptions options) {
        executeAsyncCommand(createSearch(searchString, options),
                callback, cmObjectResponseConstructor());
    }

    /**
     * Asynchronously insert the object. If it already exists in CloudMine, its contents will be replaced entirely
     * @param toCreate the object to save
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation This ordinarily should not occur
     */
    public void asyncInsert(CMObject toCreate) throws ConversionException {
        asyncInsert(toCreate, CMCallback.<ObjectModificationResponse>doNothing());
    }

    /**
     * Asynchronously insert the object. If it already exists in CloudMine, its contents will be replaced entirely
     * @param toCreate the object to save
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation This ordinarily should not occur
     */
    public void asyncInsert(CMObject toCreate, Callback<ObjectModificationResponse> callback) throws ConversionException {
        asyncInsert(toCreate, callback, CMRequestOptions.NONE);
    }

    /**
     * Asynchronously insert the object. If it already exists in CloudMine, its contents will be replaced entirely
     * @param toCreate the object to save
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation This ordinarily should not occur
     */
    public void asyncInsert(CMObject toCreate, Callback<ObjectModificationResponse> callback, CMRequestOptions options) throws ConversionException {
        executeAsyncCommand(
                createPut(toCreate.transportableRepresentation(), options),
                callback, objectModificationResponseConstructor());
    }

    /**
     * Asynchronously insert all of the objects. If any already exists in CloudMine, its contents will be replaced entirely
     * @param toCreate the objects to save
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation This ordinarily should not occur
     */
    public void asyncInsert(Collection<? extends CMObject> toCreate) throws ConversionException {
        asyncInsert(toCreate, CMCallback.<ObjectModificationResponse>doNothing());
    }

    /**
     * Asynchronously insert all of the objects. If any already exists in CloudMine, its contents will be replaced entirely
     * @param toCreate the objects to save
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation This ordinarily should not occur
     */
    public void asyncInsert(Collection<? extends CMObject> toCreate, Callback<ObjectModificationResponse> callback) throws ConversionException {
        asyncInsert(toCreate, callback, CMRequestOptions.NONE);
    }

    /**
     * Asynchronously insert all of the objects. If any already exists in CloudMine, its contents will be replaced entirely
     * @param toCreate the objects to save
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation This ordinarily should not occur
     */
    public void asyncInsert(Collection<? extends CMObject> toCreate, Callback<ObjectModificationResponse> callback, CMRequestOptions options) throws ConversionException {
        List<Transportable> transportables = new ArrayList<Transportable>(toCreate.size());
        for(CMObject object : toCreate) {
            transportables.add(new TransportableString(object.asKeyedObject()));
        }
        String jsonStringsCollection = JsonUtilities.jsonCollection(
                transportables.toArray(new Transportable[transportables.size()])
        ).transportableRepresentation();
        executeAsyncCommand(createPut(jsonStringsCollection, options),
                callback, objectModificationResponseConstructor());
    }

    /**
     * Asynchronously update the object. If it already exists in CloudMine, its contents will be merged
     * @param toUpdate the object to update
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation This ordinarily should not occur
     */
    public void asyncUpdate(CMObject toUpdate) throws ConversionException {
        asyncUpdate(toUpdate, CMCallback.<ObjectModificationResponse>doNothing());
    }

    /**
     * Asynchronously update the object. If it already exists in CloudMine, its contents will be merged
     * @param toUpdate the object to update
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation This ordinarily should not occur
     */
    public void asyncUpdate(CMObject toUpdate, Callback<ObjectModificationResponse> callback) throws ConversionException {
        executeAsyncCommand(createJsonPost(toUpdate.transportableRepresentation()), callback, objectModificationResponseConstructor());
    }

    /**
     * Asynchronously update all of the objects. If any already exists in CloudMine, its contents will be merged
     * @param objects the objects to update
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation This ordinarily should not occur
     */
    public void asyncUpdate(Collection<? extends CMObject> objects) throws ConversionException {
        asyncUpdate(objects, CMCallback.<ObjectModificationResponse>doNothing());
    }

    /**
     * Asynchronously update all of the objects. If any already exists in CloudMine, its contents will be merged
     * @param objects the objects to update
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @throws ConversionException if unable to convert to transportable representation; this should not happen unless you are subclassing objects and doing something you shouldn't be with overriding transportableRepresentation This ordinarily should not occur
     */
    public void asyncUpdate(Collection<? extends CMObject> objects, Callback<ObjectModificationResponse> callback) throws ConversionException {
        String[] jsonStrings = new String[objects.size()];
        int i = 0;
        for(CMObject cmObject : objects) {
            jsonStrings[i] = cmObject.asKeyedObject();
            i++;
        }
        String json = JsonUtilities.jsonCollection(jsonStrings).transportableRepresentation();
        executeAsyncCommand(createJsonPost(json), callback, objectModificationResponseConstructor());
    }

    /**
     * See {@link #asyncCreateUser(com.cloudmine.api.JavaCMUser, com.cloudmine.api.rest.callbacks.Callback)}
     */
    public void asyncCreateUser(JavaCMUser user)  {
        executeAsyncCommand(createPut(user));
    }

    /**
     * Create a new user
     * @param user the user to create
     * @param callback a Callback that expects a {@link CreationResponse}. It is recommended that a {@link com.cloudmine.api.rest.callbacks.CreationResponseCallback} is given here
     */
    public void asyncCreateUser(JavaCMUser user, Callback<CreationResponse> callback) {
        executeAsyncCommand(createPut(user), user.setObjectIdOnCreation(callback), creationResponseConstructor());
    }

    /**
     * Change the user's e-mail address
     * @param oldEmail
     * @param currentPassword
     * @param newEmail
     * @param callback
     */
    public void asyncChangeEmail(String oldEmail, String currentPassword, String newEmail, Callback<CMResponse> callback) {
        HttpPost updateEmail = createUpdateEmail(oldEmail, currentPassword, newEmail);
        executeAsyncCommand(updateEmail, callback);
    }

    /**
     * Change the user's name
     * @param oldEmail
     * @param currentPassword
     * @param newEmail
     * @param responseCallback
     */
    public void asyncChangeUserName(String oldEmail, String currentPassword, String newEmail, Callback<CMResponse> responseCallback) {
        HttpPost updateUserName = createUpdateUserName(oldEmail, currentPassword, newEmail);
        executeAsyncCommand(updateUserName, responseCallback);
    }

    /**
     * Change the given user's password to newPassword
     * @param user the user whose password is to be changed
     * @param newPassword the new password
     */
    public void asyncChangePassword(JavaCMUser user, String newPassword) {
        asyncChangePassword(user, newPassword, CMCallback.<CMResponse>doNothing());
    }

    /**
     * Change the given user's password to newPassword
     * @param user the user whose password is to be changed
     * @param newPassword the new password
     * @param callback a Callback that expects a CMResponse. It is recommended that a {@link com.cloudmine.api.rest.callbacks.CMResponseCallback} is given here
     */
    public void asyncChangePassword(JavaCMUser user, String newPassword, Callback<CMResponse> callback) {
        asyncChangePassword(user, newPassword, CMRequestOptions.NONE, callback);
    }

    /**
     * Change the given user's password to newPassword
     * @param user the user whose password is to be changed
     * @param newPassword the new password
     * @param callback a Callback that expects a CMResponse. It is recommended that a {@link com.cloudmine.api.rest.callbacks.CMResponseCallback} is given here
     */
    public void asyncChangePassword(JavaCMUser user, String newPassword, CMRequestOptions options, Callback<CMResponse> callback) {
        asyncChangePassword(user.getEmail(), user.getUserName(), user.getPassword(), newPassword, options, callback);
    }

    public void asyncChangePassword(String email, String oldPassword, String newPassword, CMRequestOptions options, Callback<CMResponse> callback) {
        asyncChangePassword(email, null, oldPassword, newPassword, options, callback);
    }

    public void asyncChangePassword(String email, String userName, String oldPassword, String newPassword, CMRequestOptions options, Callback<CMResponse> callback) {
        executeAsyncCommand(createChangePassword(email, userName, oldPassword, newPassword, options), callback);
    }

    /**
     * Asynchronously Request that the user with the given e-mail address's password is reset. This will generate a password reset e-mail that will be sent to the user
     * @param email the e-mail address of the user
     */
    public void asyncResetPasswordRequest(String email) {
        asyncResetPasswordRequest(email, CMCallback.<CMResponse>doNothing());
    }

    /**
     * Asynchronously Request that the user with the given e-mail address's password is reset. This will generate a password reset e-mail that will be sent to the user
     * @param email the e-mail address of the user
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link CMResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.CMResponseCallback} is passed in
     */
    public void asyncResetPasswordRequest(String email, Callback<CMResponse> callback) {
        executeAsyncCommand(createResetPassword(email), callback);
    }

    /**
     * Asynchronously confirm that a users password should be reset. Requires the token sent to the user's email address
     * @param token from the e-mail sent to the user
     * @param newPassword the new password
     */
    public void asyncResetPasswordConfirmation(String token, String newPassword) {
        asyncResetPasswordConfirmation(token, newPassword, CMCallback.<CMResponse>doNothing());
    }

    /**
     * Asynchronously confirm that a users password should be reset. Requires the token sent to the user's email address
     * @param token from the e-mail sent to the user
     * @param newPassword the new password
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link CMResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.CMResponseCallback} is passed in
     */
    public void asyncResetPasswordConfirmation(String token, String newPassword, Callback<CMResponse> callback) {
        executeAsyncCommand(createResetPasswordConfirmation(token, newPassword), callback);
    }

    /**
     * Internal method used for completing the social log in process after a redirect
     * @param challenge
     * @param callback
     */
    public void asyncCompleteSocialLogin(String challenge, Callback<CMSocialLoginResponse> callback) {
        HttpGet get = createCompleteSocialGet(challenge);
        executeAsyncCommand(get, callback, CMSocialLoginResponse.CONSTRUCTOR);
    }

    /**
     * Asynchronously log in this user
     * @param user the user to log in
     */
    public void asyncLogin(JavaCMUser user) {
        asyncLogin(user, CMCallback.<LoginResponse>doNothing());
    }

    /**
     * Asynchronously log in this user
     * NOTE: It is recommended that {@link com.cloudmine.api.JavaCMUser#login(com.cloudmine.api.rest.callbacks.Callback)} is used instead of this method,
     * as it will set the user's CMSessionToken properly
     * @param user the user to log in
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link LoginResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.LoginResponseCallback} is passed in
     */
    public void asyncLogin(JavaCMUser user, Callback<LoginResponse> callback) {
        if(user.isLoggedIn()) {
            callback.onCompletion(user.createFakeLoginResponse());
        } else {
            executeAsyncCommand(createLoginPost(user), callback, logInResponseConstructor());
        }
    }

    /**
     * Invalidate the given session token. Note that if other session tokens exist for this user, they will still be valid
     * @param token the token to invalidate
     */
    public void asyncLogout(CMSessionToken token) {
        asyncLogout(token, CMCallback.<CMResponse>doNothing());
    }

    /**
     * Invalidate the given session token. Note that if other session tokens exist for this user, they will still be valid
     * @param token the token to invalidate
     * @param callback a {@link Callback} that expects a {@link CMResponse}. It is recommended that a {@link com.cloudmine.api.rest.callbacks.CMResponseCallback} is used here
     */
    public void asyncLogout(CMSessionToken token, Callback<CMResponse> callback) {
        executeAsyncCommand(createLogoutPost(token), callback, cmResponseConstructor());
    }

    /**
     * Registers the given token with CloudMine, so push notifications can be sent to the device. If the UserCMWebService is
     * used, the token will be registered with the specific user.
     * @param senderID The senderID given by Google for Push Notifications
     * @param callback A {@link com.cloudmine.api.rest.callbacks.Callback} that expects a {@link TokenUpdateResponse} class.
     */
    public void registerForGCM(String senderID, Callback<TokenUpdateResponse> callback) {
        HttpPost postRequest = createRegisterGCMPost(senderID);
        executeAsyncCommand(postRequest, callback, tokenUpdateConstructor());
    }

    /**
     * Unregisters the token associated with this device. If it was registered with a specific user, that user's CMWebService should
     * be used to unregister it.
     * @param callback A {@link com.cloudmine.api.rest.callbacks.Callback} that expects a {@link TokenUpdateResponse} class.
     */
    public void unregisterForGCM(Callback<TokenUpdateResponse> callback) {
        HttpDelete deleteRequest = createDeleteToken();
        executeAsyncCommand(deleteRequest, callback, tokenUpdateConstructor());
    }

    /**
     * See {@link #asyncCreateChannel(com.cloudmine.api.CMChannel, com.cloudmine.api.rest.callbacks.Callback)}
     * @param channel
     */
    public void asyncCreateChannel(CMChannel channel) {
        asyncCreateChannel(channel, CMResponseCallback.<PushChannelResponse>doNothing());
    }

    /**
     * Creates a channel and calls into a {@link com.cloudmine.api.rest.callbacks.PushChannelResponseCallback}
     * @param channel
     * @param callback
     */
    public void asyncCreateChannel(CMChannel channel, Callback<PushChannelResponse> callback) {
        HttpPost post = createNotificationChannel(channel);
        executeAsyncCommand(post, callback, PushChannelResponse.CONSTRUCTOR);
    }

    /**
     * See {@link #asyncDeleteChannel(String, com.cloudmine.api.rest.callbacks.Callback)}
     * @param channelName
     */
    public void asyncDeleteChannel(String channelName) {
        asyncDeleteChannel(channelName, CMResponseCallback.<CMResponse>doNothing());
    }

    /**
     * Delete the specified channel then call into the given CMResponseCallback. Will return a success response even
     * if the channel doesn't exist
     * @param channelName
     * @param callback
     */
    public void asyncDeleteChannel(String channelName, Callback<CMResponse> callback) {
        executeAsyncCommand(createDeleteChannel(channelName), callback);
    }

    /**
     * See {@link #asyncSubscribeThisDeviceToChannel(String, com.cloudmine.api.rest.callbacks.Callback)}
     * @param channelName
     */
    public void asyncSubscribeThisDeviceToChannel(String channelName) {
        asyncSubscribeThisDeviceToChannel(channelName, CMResponseCallback.<PushChannelResponse>doNothing());
    }

    /**
     * Subscribe this device to receive pushes on the given channel. Only works on Android devices; is based on the value
     * returned by {@link com.cloudmine.api.DeviceIdentifier#getUniqueId()}
     * @param channelName the channel to subscribe to
     * @param responseCallback a PushChannelResponse callback
     */
    public void asyncSubscribeThisDeviceToChannel(String channelName, Callback<PushChannelResponse> responseCallback) {
        HttpPost post = createSubscribeSelf(channelName, true, false);
        executeAsyncCommand(post, responseCallback, PushChannelResponse.CONSTRUCTOR);
    }

    /**
     * See {@link #asyncSubscribeUsersToChannel(String, java.util.Collection, com.cloudmine.api.rest.callbacks.Callback)}
     * @param channelName
     * @param targets
     */
    public void asyncSubscribeUsersToChannel(String channelName, Collection<CMPushNotification.UserTarget> targets) {
        asyncSubscribeUsersToChannel(channelName, targets, CMCallback.<PushChannelResponse>doNothing());
    }

    /**
     * Subscribe the specified users to the given channel, and then call into the given PushChannelResponseCallback
     * @param channelName the name of the channel to subscribe the users to
     * @param targets the user's to subscribe
     * @param responseCallback
     */
    public void asyncSubscribeUsersToChannel(String channelName, Collection<CMPushNotification.UserTarget> targets, Callback<PushChannelResponse> responseCallback) {
        HttpPost post = createSubscribeUsers(channelName, targets);
        executeAsyncCommand(post, responseCallback, PushChannelResponse.CONSTRUCTOR);
    }

    public void asyncUnsubscribeUsersFromChannel(String channelName, Collection<String> userIds, Callback<PushChannelResponse> responseCallback) {
        HttpDelete delete = createUnsubscribeUsers(channelName, userIds);
        executeAsyncCommand(delete, responseCallback, PushChannelResponse.CONSTRUCTOR);
    }

    /**
     * Get the channel names that the user identified by the given id is subscribed to. Channel names come back as a
     * list of strings
     * @param userId user object id
     * @param callback a ListOfValuesResponseCallback of Strings
     */
    public void asyncLoadSubscribedChannelsForUser(String userId, Callback<ListOfValuesResponse<String>> callback) {
        HttpGet get = createListChannels(userId);
        executeAsyncCommand(get, callback, ListOfValuesResponse.CONSTRUCTOR());
    }

    /**
     * Get the channel names that the device identified by the given id is subscribed to. Channel names come back as a
     * list of strings. Device id can be obtained through {@link com.cloudmine.api.DeviceIdentifier#getUniqueId()}
     * @param deviceId
     * @param callback a ListOfValuesResponseCallback of Strings
     */
    public void asyncLoadSubscribedChannelsForDevice(String deviceId, Callback<ListOfValuesResponse<String>> callback) {
        HttpGet get = createListChannelsForDevice(deviceId);
        executeAsyncCommand(get, callback, ListOfValuesResponse.CONSTRUCTOR());
    }

    public void asyncLoadChannelInformation(String channelName, Callback<PushChannelResponse> callback) {

    }

    /**
     * See {@link #asyncSendNotification(com.cloudmine.api.CMPushNotification, com.cloudmine.api.rest.callbacks.Callback)}
     * @param notification
     */
    public void asyncSendNotification(CMPushNotification notification) {
        asyncSendNotification(notification, CMResponseCallback.<CMResponse>doNothing());
    }

    /**
     * Send a notification then call into the given CMResponse. A success response has no body
     * @param notification
     * @param callback
     */
    public void asyncSendNotification(CMPushNotification notification, Callback<CMResponse> callback) {
        HttpPost postRequest = createNotificationPost(notification);
        executeAsyncCommand(postRequest, callback);
    }

    /**
     * Make a blocking call to load the object associated with the given objectId
     * @param objectId of the object to load
     * @return a CMObjectResponse containing success or failure, and the loaded object if it exists and the call was a success
     * @throws NetworkException if unable to perform the request
     *
     */
    public CMObjectResponse loadObject(String objectId) throws NetworkException {
        return loadObjects(Collections.singletonList(objectId));
    }

    /**
     * Make a blocking call to load all of the objects associated with the given objectIds
     * @param objectIds of the objects to load
     * @return a CMObjectResponse containing success or failure, and the loaded objects if they exist and the call was a success
     * @throws NetworkException if unable to perform the request
     */
    public CMObjectResponse loadObjects(Collection<String> objectIds) throws NetworkException{
        return executeCommand(createGetObjects(objectIds), cmObjectResponseConstructor());
    }

    /**
     * Make a blocking call to load all of the objects
     * @return a CMObjectResponse containing success or failure, and the loaded objects if they exist and the call was a success
     * @throws NetworkException if unable to perform the request
     */
    public CMObjectResponse loadAllObjects() throws NetworkException {
        return executeCommand(createGet(), cmObjectResponseConstructor());
    }

    /**
     * Make a blocking call to load the specified file
     * @param fileId the name of the file to load
     * @return a FileLoadResponse that contains the loaded file, if the call was a success
     * @throws NetworkException if unable to perform the network call
     * @throws CreationException if unable to create the CMFile
     */
    public FileLoadResponse loadFile(String fileId) throws NetworkException, CreationException {
        try {
            HttpResponse response = httpClient.execute(createGetFile(fileId));
            return new FileLoadResponse(response, fileId);
        } catch (IOException e) {
            LOG.error("IOException getting file", e);
            throw new CreationException("Couldn't get file because of IOException", e);
        }
    }

    /**
     * Make a blocking call to search for CloudMine objects.
     * @param searchString the search string to use. For more information on syntax. See <a href="https://cloudmine.me/docs/object-storage#query_syntax">Search query syntax</a>
     * @return  the {@link com.cloudmine.api.rest.response.CMObjectResponse} containing the retrieved objects.
     * @throws NetworkException if unable to perform the network call
     */
    public CMObjectResponse loadSearch(String searchString) throws NetworkException {
        HttpGet get = createSearch(searchString);
        return executeCommand(get, cmObjectResponseConstructor());
    }

    /**
     * Make a blocking call to directly insert a transportable representation into CloudMine
     * @param transport a valid transportable representation of a CloudMine object
     * @return the ObjectModificationResponse containing success and error values
     * @throws NetworkException if unable to perform the network call
     */
    public ObjectModificationResponse insert(String transport) throws NetworkException {
        HttpPut put = createPut(transport);
        return executeCommand(put, objectModificationResponseConstructor());
    }

    /**
     * Make a blocking call to directly update an object using a transportable representation in CloudMine. If the object already exists, its values will be updated; otherwise it will be inserted
     * @param transport a valid transportable representation of a CloudMine object
     * @return the ObjectModificationResponse containing success and error values
     * @throws NetworkException if unable to perform the network call
     */
    public ObjectModificationResponse update(String transport) throws NetworkException {
        HttpPost post = createJsonPost(transport);
        return executeCommand(post, objectModificationResponseConstructor());
    }

    /**
     * Make a blocking call to insert a file
     * @param file to insert
     * @return the FileCreationResponse that indicates success and failure, and has the file name
     * @throws NetworkException if unable to perform the network call
     */
    public FileCreationResponse insert(CMFile file) throws NetworkException {
        return executeCommand(createPut(file), fileCreationResponseConstructor());
    }

    /**
     * Change the given user's password to newPassword
     * @param user the user whose password is to be changed
     * @param newPassword the new password
     * @return a CMResponse that indicates success or failure
     * @throws NetworkException if unable to perform the network call
     */
    public CMResponse changePassword(JavaCMUser user, String newPassword) throws NetworkException {
        return executeCommand(createChangePassword(user, newPassword, CMRequestOptions.NONE));
    }


    /**
     * Make a blocking call to create this user
     * @return  the {@link LoginResponse} which will include the CMSessionToken that authenticates this user and provides access to the user level store
     * @throws NetworkException if unable to perform the network call
     * @throws ConversionException if unable to convert this user to a transportable representation. This should never happen unless you have subclassed CMUser and overridden transportableRepresentation
     */
    public CMResponse insert(JavaCMUser user) throws NetworkException, ConversionException {
        return executeCommand(createPut(user));
    }

    /**
     * Make a blocking call to log in this user
     * @param user to log in
     * @return a LoginResponse that will contain the CMSessionToken used to validate user requests
     * @throws NetworkException if unable to perform the network call
     */
    public LoginResponse login(JavaCMUser user) throws NetworkException {
        if(user.isLoggedIn()) {
            return user.createFakeLoginResponse();
        }
        return executeCommand(createLoginPost(user), logInResponseConstructor());
    }

    /**
     * Make a blocking call to invalidate this sessionToken
     * @param sessionToken to invalidate
     * @return a CMResponse that indicates success or failure
     * @throws NetworkException if unable to perform the network call
     */
    public CMResponse logout(CMSessionToken sessionToken) throws NetworkException {
        return executeCommand(createLogoutPost(sessionToken));
    }

    private void executeAsyncCommand(HttpUriRequest message) {
        executeAsyncCommand(message, CMCallback.doNothing(), cmResponseConstructor());
    }

    void executeAsyncCommand(HttpUriRequest message, Callback callback) {
        executeAsyncCommand(message, callback, cmResponseConstructor());
    }

    <T> void executeAsyncCommand(HttpUriRequest message, final Callback callback, ResponseConstructor<T> constructor) {
        final long startTime = System.currentTimeMillis();
        callback.setStartTime(startTime);
        asyncHttpClient.executeCommand(message, callback, constructor);
    }

    private CMResponse executeCommand(HttpUriRequest message) throws NetworkException {
        return executeCommand(message, cmResponseConstructor());
    }

    private <T extends ResponseBase> T executeCommand(HttpUriRequest message, ResponseConstructor<T> constructor) throws NetworkException{
        HttpResponse response = null;
        try {
            response = httpClient.execute(message);
            return constructor.construct(response);
        }
        catch (IOException e) {
            LOG.error("Error executing command: " + message.getURI(), e);
            throw new NetworkException("Couldn't execute command, IOException: ", e);
        }
        finally {
            CMWebService.consumeEntityResponse(response);
        }
    }

    //**************************Http commands*****************************************


    private HttpGet createCompleteSocialGet(String challenge) {
        String url = baseUrl.copy().account().social().login().status().statusChallenge(challenge).asUrlString();
        HttpGet get = new HttpGet(url);
        addCloudMineHeader(get);
        return get;
    }

    private HttpGet createSearch(String search) {
        return createSearch(search, CMRequestOptions.NONE);
    }
    private HttpGet createSearch(String search, CMRequestOptions options) {
        HttpGet get = new HttpGet(baseUrl.copy().search(search).options(options).asUrlString());
        addCloudMineHeader(get);
        return get;
    }

    private HttpDelete createDeleteUser(String userId) {
        HttpDelete delete = new HttpDelete(baseUrl.copy().account().addKey(userId).asUrlString());
        addCloudMineHeader(delete);
        return delete;
    }

    private HttpDelete createDeleteAll() {
        HttpDelete delete = new HttpDelete(baseUrl.copy().deleteAll().asUrlString());
        addCloudMineHeader(delete);
        return delete;
    }

    private HttpDelete createDelete(String key) {
        return createDelete(key, CMRequestOptions.NONE);
    }

    private HttpDelete createDelete(String key, CMRequestOptions options) {
        HttpDelete delete = new HttpDelete(baseUrl.copy().delete(key).options(options).asUrlString());
        addCloudMineHeader(delete);
        return delete;
    }

    private HttpDelete createDelete(Collection<String> keys) {
        return createDelete(keys, CMRequestOptions.NONE);
    }

    private HttpDelete createDeleteChannel(String channelName) {
        HttpDelete delete = new HttpDelete(baseUrl.copy().push().channel().addAction(channelName).asUrlString());
        addCloudMineHeader(delete);
        return delete;
    }

    private HttpDelete createDelete(Collection<String> keys, CMRequestOptions options) {
        HttpDelete delete = new HttpDelete(baseUrl.copy().delete(keys).options(options).asUrlString());

        addCloudMineHeader(delete);
        return delete;
    }

    private HttpDelete createDeleteToken() {
        HttpDelete delete = new HttpDelete(baseUrl.copy().device().asUrlString());
        addCloudMineHeader(delete);
        return delete;
    }

    private HttpPut createPut(String json) {
        return createPut(json, CMRequestOptions.NONE);
    }
    private HttpPut createPut(String json, CMRequestOptions options) {
        HttpPut put = new HttpPut(baseUrl.copy().text().options(options).asUrlString());
        addCloudMineHeader(put);
        addJson(put, json);
        return put;
    }

    private HttpPut createPut(JavaCMUser user) throws ConversionException {
        HttpPut put = new HttpPut(baseUrl.copy().account().create().asUrlString());
        addCloudMineHeader(put);
        addJson(put, user.transportableRepresentation());
        return put;
    }

    private HttpPut createPut(CMFile file) {
        HttpPut put = new HttpPut(baseUrl.copy().binary(file.getFileId()).asUrlString());
        addCloudMineHeader(put);
        put.setEntity(new ByteArrayEntity(file.getFileContents()));
        put.addHeader("Content-Type", file.getMimeType());
        return put;
    }

    private HttpPost createNotificationPost(CMPushNotification pushNotification) {
        HttpPost post = createPost(baseUrl.copy().push().asUrlString());
        addJson(post, pushNotification.transportableRepresentation());
        return post;
    }

    private HttpPost createNotificationChannel(CMChannel channel) {
        HttpPost post = createPost(baseUrl.copy().push().channel().asUrlString());
        addJson(post, channel);
        return post;
    }

    HttpGet createListChannels(String userid) {
        CMURLBuilder partialUrl = baseUrl.copy().account().channels();
        if(Strings.isNotEmpty(userid)) partialUrl.addQuery("userid", userid);
        HttpGet get = new HttpGet(partialUrl.asUrlString());
        addCloudMineHeader(get);
        return get;
    }

    HttpGet createListChannelsForDevice(String deviceId) {
        HttpGet get = new HttpGet(baseUrl.copy().device().addAction(deviceId).channels().asUrlString());
        addCloudMineHeader(get);
        return get;
    }

    HttpPost createSubscribeSelf(String channel, boolean isDevice, boolean isUser) {
        HttpPost post = createPost(baseUrl.copy().notUser().push().channel().addAction(channel).subscribe().asUrlString());
        addJson(post,
                JsonUtilities.wrap(
                        JsonUtilities.createJsonProperty("user", isUser) + ", " +
                        JsonUtilities.createJsonProperty("device", isDevice)
                )
        );
        return post;
    }

    private static final String IS_USER_JSON = JsonUtilities.wrap(
            JsonUtilities.createJsonProperty("user", true)
    );
    HttpPost createUnsubscribeSelf(String channel) {
        HttpPost post = createPost(baseUrl.copy().notUser().push().channel().addAction(channel).unsubscribe().asUrlString());
        addJson(post, IS_USER_JSON);
        return post;
    }

    private HttpDelete createUnsubscribeUsers(String channelName, Collection<String> objectIds) {
        HttpDelete delete = new HttpDelete(baseUrl.copy().push().channel().addAction(channelName).userIds().ids(objectIds).asUrlString());
        addCloudMineHeader(delete);
        return delete;
    }

    private HttpPost createSubscribeUsers(String channel, Collection<CMPushNotification.UserTarget> targets) {
        HttpPost post = createPost(baseUrl.copy().push().channel().addAction(channel).users().asUrlString());
        String json = JsonUtilities.objectToJson(targets);
        addJson(post, json);
        return post;
    }

    private HttpPost createJsonPost(String json) {
        HttpPost post = createPost(baseUrl.copy().text().asUrlString());
        addJson(post, json);
        return post;
    }

    private HttpPost createLoginPost(JavaCMUser user) {
        HttpPost post = createPost(baseUrl.copy().account().login().asUrlString());
        addAuthorizationHeader(user, post);
        return post;
    }

    private HttpPost createLogoutPost(CMSessionToken sessionToken) {
        HttpPost post = createPost(baseUrl.copy().account().logout().asUrlString());
        post.addHeader("X-CloudMine-SessionToken", sessionToken.getSessionToken());
        return post;
    }

    private HttpPost createUpdateEmail(String oldEmail, String currentPassword, String newEmail) {
        HttpPost post = createPost(baseUrl.copy().account().credentials().asUrlString());
        addAuthorizationHeader(oldEmail, null, currentPassword, post);
        addJson(post, JsonUtilities.wrap(JsonUtilities.createJsonProperty(JavaCMUser.EMAIL_KEY, newEmail)));
        return post;
    }

    private HttpPost createUpdateUserName(String oldUserName, String currentPassword, String newUserName) {
        HttpPost post = createPost(baseUrl.copy().account().credentials().asUrlString());
        addAuthorizationHeader(null, oldUserName, currentPassword, post);
        addJson(post, JsonUtilities.wrap(JsonUtilities.createJsonProperty(JavaCMUser.USERNAME_KEY, newUserName)));
        return post;
    }

    private HttpPost createRegisterGCMPost(String senderID) {
        HttpPost post = createPost(baseUrl.copy().device().asUrlString());
        Map<String, String> body = new HashMap<String, String>();
        body.put("registration_id", senderID);
        addJson(post, JsonUtilities.mapToJson(body));
        return post;
    }

    private HttpPost createPost(String url){
        HttpPost post = new HttpPost(url);
        addCloudMineHeader(post);
        return post;
    }

    private HttpGet createGet() {
        HttpGet get = new HttpGet(baseUrl.copy().text().asUrlString());
        addCloudMineHeader(get);
        return get;
    }

    HttpGet createGet(String url) {
        HttpGet get = new HttpGet(url);
        addCloudMineHeader(get);
        return get;
    }

    private HttpGet createGetFile(String key) {
        return createGetFile(key, CMRequestOptions.NONE);
    }

    private HttpGet createGetFile(String key, CMRequestOptions options) {
        HttpGet get = new HttpGet(baseUrl.copy().binary(key).options(options).asUrlString());
        addCloudMineHeader(get);
        return get;
    }

    private HttpGet createGetFileMetaData(String fileId, CMRequestOptions options) {
        HttpGet get = new HttpGet(baseUrl.copy().search(createFileMetaDataSearch(fileId)).options(options).asUrlString());
        addCloudMineHeader(get);
        return get;
    }

    private String createFileMetaDataSearch(String fileId) {
        return new StringBuilder("[").append(JsonUtilities.TYPE_KEY).append(" = \"").append(CMFile.TYPE_VALUE).append("\", ")
                .append(JsonUtilities.OBJECT_ID_KEY).append(" = \"").append(fileId).append("\"]").toString();
    }

    private HttpGet createGetAllUsers() {
        HttpGet get = new HttpGet(baseUrl.copy().account().asUrlString());
        addCloudMineHeader(get);
        return get;
    }

    private HttpGet createProfileSearch(String searchString) {
        return createProfileSearch(searchString, CMRequestOptions.NONE);
    }

    private HttpGet createProfileSearch(String searchString, CMRequestOptions options) {
        return createGet(baseUrl.copy().account().search(searchString, "p").options(options).asUrlString());
    }

    private HttpGet createGetObjects(Collection<String> keys) {
        return createGetObjects(keys, CMRequestOptions.NONE);
    }

    private HttpGet createGetObjects(Collection<String> keys, CMRequestOptions options) {
        HttpGet get = new HttpGet(baseUrl.copy().text().objectIds(keys).options(options).asUrlString());
        addCloudMineHeader(get);
        return get;
    }

    private HttpPost createResetPasswordConfirmation(String token, String newPassword) {
        HttpPost post = new HttpPost(baseUrl.copy().account().password().reset().addAction(token).asUrlString());
        addCloudMineHeader(post);
        try {
            addJson(post, JsonUtilities.jsonCollection(
                    JsonUtilities.createJsonProperty(PASSWORD_KEY, newPassword)
            ));
        } catch (ConversionException e) {
            LOG.error("Unable to create jsoncollection", e); //this should not happen ever so we swallow
        }
        return post;
    }

    private HttpPost createResetPassword(String email) {
        HttpPost post = new HttpPost(baseUrl.copy().account().password().reset().asUrlString());
        addCloudMineHeader(post);
        try {
            addJson(post, JsonUtilities.jsonCollection(
                    JsonUtilities.createJsonProperty(EMAIL_KEY, email)
            ));
        } catch (ConversionException e) {
            LOG.error("Unable to create json collection from email key", e); //this should never happen
        }
        return post;
    }

    private HttpPost createChangePassword(JavaCMUser user, String newPassword, CMRequestOptions options) {
        return createChangePassword(user.getEmail(), user.getUserName(), user.getPassword(), newPassword, options);
    }

    private HttpPost createChangePassword(String email, String userName, String oldPassword, String newPassword, CMRequestOptions options) {
        HttpPost post = new HttpPost(baseUrl.copy().account().password().change().options(options).asUrlString());
        addCloudMineHeader(post);
        addAuthorizationHeader(email, userName, oldPassword, post);
        try {
            addJson(post, JsonUtilities.jsonCollection(
                    JsonUtilities.createJsonProperty(PASSWORD_KEY, newPassword)));
        } catch (ConversionException e) {
            LOG.error("Unable to create json collection from property", e); //this should never happen
        }
        return post;
    }

    protected void addJson(HttpEntityEnclosingRequestBase message, String json) {
        if(json == null)
            json = JsonUtilities.EMPTY_JSON;
        if(!message.containsHeader(JSON_HEADER.getName())) {
            message.addHeader(JSON_HEADER);
        }
        try {
            message.setEntity(new StringEntity(json, JSON_ENCODING));
        } catch (UnsupportedEncodingException e) {
            LOG.error("Error encoding json", e);
        }
    }

    private void addJson(HttpEntityEnclosingRequestBase message, Transportable transportable) throws ConversionException {
        addJson(message, transportable.transportableRepresentation());
    }

    protected void addAuthorizationHeader(JavaCMUser user, HttpEntityEnclosingRequestBase post) {
        addAuthorizationHeader(user.getEmail(), user.getUserName(), user.getPassword(), post);
    }

    protected void addAuthorizationHeader(String email, String userName, String password, HttpEntityEnclosingRequestBase post) {
        String authName = Strings.isNotEmpty(email) ? email : userName;
        post.addHeader(AUTHORIZATION_KEY, "Basic " + JavaCMUser.encode(authName, password));
    }

    protected void addCloudMineHeader(AbstractHttpMessage message) {
        for(Header header : LibrarySpecificClassCreator.getCreator().getHeaderFactory().getCloudMineHeaders()) {
            message.addHeader(header);
        }
    }

    //**********************RESPONSE CONSTRUCTORS******************************
    protected ResponseConstructor<FileCreationResponse> fileCreationResponseConstructor() {
        return FileCreationResponse.CONSTRUCTOR;
    }

    protected ResponseConstructor<ObjectModificationResponse> objectModificationResponseConstructor() {
        return ObjectModificationResponse.CONSTRUCTOR;
    }

    protected ResponseConstructor<CreationResponse> creationResponseConstructor() {
        return CreationResponse.CONSTRUCTOR;
    }

    protected ResponseConstructor<CMResponse> cmResponseConstructor() {
        return CMResponse.CONSTRUCTOR;
    }

    protected ResponseConstructor<FileLoadResponse> fileLoadResponseResponseConstructor(String key) {
        return FileLoadResponse.constructor(key);
    }

    protected ResponseConstructor<LoginResponse> logInResponseConstructor() {
        return LoginResponse.CONSTRUCTOR;
    }

    protected ResponseConstructor<CMObjectResponse> cmObjectResponseConstructor() {
        return CMObjectResponse.CONSTRUCTOR;
    }

    protected ResponseConstructor<TokenUpdateResponse> tokenUpdateConstructor() {
        return TokenUpdateResponse.CONSTRUCTOR;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CMWebService that = (CMWebService) o;

        if (!asyncHttpClient.equals(that.asyncHttpClient)) return false;
        if (!baseUrl.copy().equals(that.baseUrl)) return false;
        if (!httpClient.equals(that.httpClient)) return false;
        if (loggedInSessionToken != null ? !loggedInSessionToken.equals(that.loggedInSessionToken) : that.loggedInSessionToken != null)
            return false;
        if (loggedInUserServices != null ? !loggedInUserServices.equals(that.loggedInUserServices) : that.loggedInUserServices != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = baseUrl.copy().hashCode();
        result = 31 * result + httpClient.hashCode();
        result = 31 * result + asyncHttpClient.hashCode();
        result = 31 * result + (loggedInSessionToken != null ? loggedInSessionToken.hashCode() : 0);
        result = 31 * result + (loggedInUserServices != null ? loggedInUserServices.hashCode() : 0);
        return result;
    }
}

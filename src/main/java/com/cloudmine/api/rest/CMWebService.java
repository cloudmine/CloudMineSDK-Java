package com.cloudmine.api.rest;

import com.cloudmine.api.*;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.exceptions.JsonConversionException;
import com.cloudmine.api.exceptions.NetworkException;
import com.cloudmine.api.rest.callbacks.Callback;
import com.cloudmine.api.rest.response.*;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.Future;

/**
 * Provides direct access to the CloudMine API. Useful if you don't need all the bookkeeping of a {@link CMStore}. Also
 * provides some synchronous implementations of API calls, for when you know you are not on the UI thread.
 * This base class performs all operations at the Application level. To perform operations at the User level, use a
 * {@link UserCMWebService}<br>
 * Preconditions for use:<br>
 * {@link DeviceIdentifier#initialize(android.content.Context)} has been called with the activity context<br>
 * {@link CMApiCredentials#initialize(String, String)} has been called with the application identifier and API key<br>

 * Copyright CloudMine LLC
 */
public class CMWebService {

    private static final Logger LOG = LoggerFactory.getLogger(CMWebService.class);
    public static final Header JSON_HEADER = new BasicHeader("Content-Type", "application/json");
    public static final String AGENT_HEADER_KEY = "X-CloudMine-Agent";
    public static final String PASSWORD_KEY = "password";
    public static final String JSON_ENCODING = "UTF-8";
    public static final String AUTHORIZATION_KEY = "Authorization";
    public static final String CLOUD_MINE_AGENT = "javasdk 1.0";
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
        return AndroidCMWebService.getService(); //This will be returned for the android library
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
     * @param token a session token return from a valid login request
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
            userService = UserCMWebService.UserCMWebService(baseUrl.user(), token, asyncHttpClient);
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
     * Get the 'default' user web service. Requires that setLoggedInUser has been called
     * @return the UserCMWebService for the logged in user
     * @throws CreationException if setLoggedInUser has not been called, or was called with an invalid value
     */
    public synchronized UserCMWebService getUserWebService() throws CreationException {
        if(loggedInSessionToken == null) {
            throw new CreationException("Cannot request a user web service until setLoggedInUser has been called");
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
     * @param klass the class of the objects to load; you must call {@link SimpleCMObject#setClass(String)} before persisting for this property to exist on your objects
     * @return a Future containing the SimpleCMObjectResponse that will provide access to the loaded objects
     */
    public Future<SimpleCMObjectResponse> asyncLoadObjectsOfClass(String klass) {
        return asyncLoadObjectsOfClass(klass, Callback.DO_NOTHING);
    }

    /**
     * Load all of the objects of the specified class
     * @param klass the class of the objects to load; you must call {@link SimpleCMObject#setClass(String)} before persisting for this property to exist on your objects
     * @param callback
     * @return a Future containing the SimpleCMObjectResponse that will provide access to the loaded objects
     */
    public Future<SimpleCMObjectResponse> asyncLoadObjectsOfClass(String klass, Callback callback) {
        return asyncLoadObjectsOfClass(klass, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the objects that are of the specified class. Class values are set using
     * {@link SimpleCMObject#setClass(String)}
     * @param klass the class type to load
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used here
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> asyncLoadObjectsOfClass(String klass, Callback callback, CMRequestOptions options) {
        HttpGet search = createSearch("[" + JsonUtilities.CLASS_KEY + "=" + JsonUtilities.addQuotes(klass) + "]");
        return executeAsyncCommand(addRequestOptions(search, options),
                callback, simpleCMObjectResponseConstructor());
    }

    /**
     * Delete the given object from CloudMine.
     * @param object to delete; this is done based on the object id, its values are ignored
     * @return a Future containing the {@link ObjectModificationResponse} which can be queried to check the success of this operation
     */
    public Future<ObjectModificationResponse> asyncDeleteObject(SimpleCMObject object) {
        return asyncDeleteObject(object, Callback.DO_NOTHING);
    }

    /**
     * Delete the given object from CloudMine.
     * @param object to delete; this is done based on the object id, its values are ignored
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @return a Future containing the {@link ObjectModificationResponse} which can be queried to check the success of this operation
     */
    public Future<ObjectModificationResponse> asyncDeleteObject(SimpleCMObject object, Callback callback) {
        return asyncDeleteObject(object, callback, CMRequestOptions.NONE);
    }

    /**
     * Delete the given object from CloudMine.
     * @param object to delete; this is done based on the object id, its values are ignored
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     * @return a Future containing the {@link ObjectModificationResponse} which can be queried to check the success of this operation
     */
    public Future<ObjectModificationResponse> asyncDeleteObject(SimpleCMObject object, Callback callback, CMRequestOptions options) {
        return asyncDeleteObjects(Collections.singletonList(object), callback, options);
    }

    /**
     * Delete the given objects from CloudMine.
     * @param objects to delete; this is done based on the object ids, values are ignored
     * @return a Future containing the {@link ObjectModificationResponse} which can be queried to check the success of this operation
     */
    public Future<ObjectModificationResponse> asyncDeleteObjects(Collection<SimpleCMObject> objects) {
        return asyncDeleteObjects(objects, Callback.DO_NOTHING);
    }

    /**
     * Delete the given objects from CloudMine.
     * @param objects to delete; this is done based on the object ids, values are ignored
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @return a Future containing the {@link ObjectModificationResponse} which can be queried to check the success of this operation
     */
    public Future<ObjectModificationResponse> asyncDeleteObjects(Collection<SimpleCMObject> objects, Callback callback) {
        return asyncDeleteObjects(objects, callback, CMRequestOptions.NONE);
    }

    /**
     * Delete the given objects from CloudMine.
     * @param objects to delete; this is done based on the object ids, values are ignored
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     * @return a Future containing the {@link ObjectModificationResponse} which can be queried to check the success of this operation
     */
    public Future<ObjectModificationResponse> asyncDeleteObjects(Collection<SimpleCMObject> objects, Callback callback, CMRequestOptions options) {
        int size = objects.size();
        Collection<String> keys = new ArrayList<String>(size);
        for(SimpleCMObject object : objects) {
            keys.add(object.getObjectId());
        }
        return asyncDelete(keys, callback, options);
    }

    /**
     * Delete the given object from CloudMine.
     * @param objectId to delete; this is done based on the object id
     * @return a Future containing the {@link ObjectModificationResponse} which can be queried to check the success of this operation
     */
    public Future<ObjectModificationResponse> asyncDelete(String objectId) {
        return asyncDelete(objectId, Callback.DO_NOTHING);
    }

    /**
     * Delete the given object from CloudMine.
     * @param objectId to delete; this is done based on the object id
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @return a Future containing the {@link ObjectModificationResponse} which can be queried to check the success of this operation
     */
    public Future<ObjectModificationResponse> asyncDelete(String objectId, Callback callback) {
        return asyncDelete(objectId, callback, CMRequestOptions.NONE);
    }

    /**
     * Delete the given object from CloudMine.
     * @param objectId to delete; this is done based on the object id
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     * @return a Future containing the {@link ObjectModificationResponse} which can be queried to check the success of this operation
     */
    public Future<ObjectModificationResponse> asyncDelete(String objectId, Callback callback, CMRequestOptions options) {
        return asyncDelete(Collections.singletonList(objectId), callback, options);
    }

    /**
     * Delete the given objects from CloudMine.
     * @param objectIds to delete; this is done based on the object ids
     * @return a Future containing the {@link ObjectModificationResponse} which can be queried to check the success of this operation
     */
    public Future<ObjectModificationResponse> asyncDelete(Collection<String> objectIds) {
        return asyncDelete(objectIds, Callback.DO_NOTHING);
    }

    /**
     * Delete the given objects from CloudMine.
     * @param objectIds to delete; this is done based on the object ids
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @return a Future containing the {@link ObjectModificationResponse} which can be queried to check the success of this operation
     */
    public Future<ObjectModificationResponse> asyncDelete(Collection<String> objectIds, Callback callback) {
        return asyncDelete(objectIds, callback, CMRequestOptions.NONE);
    }

    /**
     * Delete the given objects from CloudMine.
     * @param objectIds to delete; this is done based on the object ids
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     * @return a Future containing the {@link ObjectModificationResponse} which can be queried to check the success of this operation
     */
    public Future<ObjectModificationResponse> asyncDelete(Collection<String> objectIds, Callback callback, CMRequestOptions options) {
        return executeAsyncCommand(addRequestOptions(createDelete(objectIds), options),
                callback, objectModificationResponseConstructor());
    }

    /**
     * This will delete ALL the objects associated with this API key. Be careful...
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @returna Future containing the {@link ObjectModificationResponse} which can be queried to check the success of this operation
     */
    public Future<ObjectModificationResponse> asyncDeleteAll(Callback callback) {
        return executeAsyncCommand(createDeleteAll(), callback, objectModificationResponseConstructor());
    }

    /**
     * This will delete ALL the objects associated with this API key. Be careful...
     * @returna Future containing the {@link ObjectModificationResponse} which can be queried to check the success of this operation
     */
    public Future<ObjectModificationResponse> asyncDeleteAll() {
        return asyncDeleteAll(Callback.DO_NOTHING);
    }

    /**
     * Delete the CMFile
     * @param file the file to delete
     * @return a Future containing the {@link ObjectModificationResponse}
     */
    public Future<ObjectModificationResponse> asyncDeleteFile(CMFile file) {
        return asyncDeleteFile(file, Callback.DO_NOTHING);
    }
    /**
     * Delete the CMFile
     * @param file the file to delete
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link ObjectModificationResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in
     * @return a Future containing the {@link ObjectModificationResponse}
     */
    public Future<ObjectModificationResponse> asyncDeleteFile(CMFile file, Callback callback) {
        return asyncDelete(file.getFileName(), callback);
    }

    /**
     * Delete all the given CMFiles
     * @param files the files to delete
     * @return a Future containing the {@link ObjectModificationResponse}
     */
    public Future<ObjectModificationResponse> asyncDeleteFiles(Collection<CMFile> files) {
        return asyncDeleteFiles(files, Callback.DO_NOTHING);
    }

    /**
     * Delete all the given CMFiles
     * @param files the files to delete
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link ObjectModificationResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in
     * @return a Future containing the {@link ObjectModificationResponse}
     */
    public Future<ObjectModificationResponse> asyncDeleteFiles(Collection<CMFile> files, Callback callback) {
        Collection<String> keys = new ArrayList<String>(files.size());
        for(CMFile file : files) {
            keys.add(file.getFileName());
        }
        return asyncDelete(keys, callback);
    }

    /**
     * Delete the {@link CMFile} with the specified fileName
     * @param fileName the file fileName, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @return a Future containing the {@link ObjectModificationResponse}
     */
    public Future<ObjectModificationResponse> asyncDeleteFile(String fileName) {
        return asyncDeleteFile(fileName, Callback.DO_NOTHING);
    }

    /**
     * Delete the {@link CMFile} with the specified fileName
     * @param fileName the file fileName, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link ObjectModificationResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in
     * @return a Future containing the {@link ObjectModificationResponse}
     */
    public Future<ObjectModificationResponse> asyncDeleteFile(String fileName, Callback callback) {
        return asyncDeleteFile(fileName, callback, CMRequestOptions.NONE);
    }

    /**
     * Delete the {@link CMFile} with the specified fileName
     * @param fileName the file fileName, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link ObjectModificationResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     * @return a Future containing the {@link ObjectModificationResponse}
     */
    public Future<ObjectModificationResponse> asyncDeleteFile(String fileName, Callback callback, CMRequestOptions options) {
        return asyncDelete(fileName, callback, options);
    }

    /**
     * Add the given file to CloudMine
     * @param file the file to add
     * @return a Future containing the FileCreationResponse that the server will return
     */
    public Future<FileCreationResponse> asyncUpload(CMFile file) {
        return asyncUpload(file, Callback.DO_NOTHING);
    }

    /**
     * Add the given file to CloudMine
     * @param file the file to add
     * @param callback a {@link Callback} that expects a {@link FileCreationResponse}. It is recommended that you pass in a {@link com.cloudmine.api.rest.callbacks.FileCreationResponseCallback}
     * @return a Future containing the FileCreationResponse that the server will return
     */
    public Future<FileCreationResponse> asyncUpload(CMFile file, Callback callback) {
        return executeAsyncCommand(createPut(file), callback, fileCreationResponseConstructor());
    }

    /**
     * Retrieve the {@link CMFile} with the specified fileName, if it exists
     * @param fileName the file fileName, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @return a Future containing the {@link FileLoadResponse}
     */
    public Future<FileLoadResponse> asyncLoadFile(String fileName) {
        return asyncLoadFile(fileName, Callback.DO_NOTHING);
    }

    /**
     * Retrieve the {@link CMFile} with the specified fileName, if it exists
     * @param fileName the file fileName, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects a FileLoadResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.FileLoadCallback} is passed in
     * @return a Future containing the {@link FileLoadResponse}
     */
    public Future<FileLoadResponse> asyncLoadFile(String fileName, Callback callback) {
        return asyncLoadFile(fileName, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve the {@link CMFile} with the specified fileName, if it exists
     * @param fileName the file fileName, either specified when the CMFile was instantiated or returned in the {@link com.cloudmine.api.rest.response.FileCreationResponse} post insertion
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects a FileLoadResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.FileLoadCallback} is passed in
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     * @return a Future containing the {@link FileLoadResponse}
     */
    public Future<FileLoadResponse> asyncLoadFile(String fileName, Callback callback, CMRequestOptions options) {
        return executeAsyncCommand(addRequestOptions(createGetFile(fileName), options),
                callback, fileLoadResponseResponseConstructor(fileName));
    }

    /**
     * Retrieve all the objects
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> asyncLoadObjects() {
        return asyncLoadObjects(Callback.DO_NOTHING);
    }

    /**
     * Retrieve all the objects
     * @param callback a Callback that expects a {@link SimpleCMObjectResponse}. It is recommended that a {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> asyncLoadObjects(Callback callback) {
        return asyncLoadObjects(callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the objects
     * @param callback a Callback that expects a {@link SimpleCMObjectResponse}. It is recommended that a {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> asyncLoadObjects(Callback callback, CMRequestOptions options) {
        return asyncLoadObjects(Collections.<String>emptyList(), callback, options);
    }

    /**
     * Retrieve all the object with the given objectId
     * @param objectId the top level objectId of the object to retrieve
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> asyncLoadObject(String objectId) {
        return asyncLoadObject(objectId, Callback.DO_NOTHING);
    }

    /**
     * Retrieve all the object with the given objectId
     * @param objectId the top level objectId of the object to retrieve
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used here
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> asyncLoadObject(String objectId, Callback callback) {
        return asyncLoadObjects(Collections.<String>singleton(objectId), callback);
    }

    /**
     * Retrieve all the objects with the given objectIds
     * @param objectIds the top level objectIds of the objects to retrieve
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> asyncLoadObjects(Collection<String> objectIds) {
        return asyncLoadObjects(objectIds, Callback.DO_NOTHING);
    }

    /**
     * Retrieve all the objects with the given objectIds
     * @param objectIds the top level objectIds of the objects to retrieve
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used here
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> asyncLoadObjects(Collection<String> objectIds, Callback callback) {
        return asyncLoadObjects(objectIds, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the objects with the given objectIds
     * @param objectIds the top level objectIds of the objects to retrieve
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used here
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> asyncLoadObjects(Collection<String> objectIds, Callback callback, CMRequestOptions options) {
        return executeAsyncCommand(addRequestOptions(createGetObjects(objectIds), options),
                callback, simpleCMObjectResponseConstructor());
    }

    /**
     * Retrieve all the objects that match the given search
     * @param searchString the search string to use. For more information on syntax. See <a href="https://cloudmine.me/docs/api-reference#ref/query_syntax">Search query syntax</a>
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> asyncSearch(String searchString) {
        return asyncSearch(searchString, Callback.DO_NOTHING);
    }

    /**
     * Retrieve all the objects that match the given search
     * @param searchString the search string to use. For more information on syntax. See <a href="https://cloudmine.me/docs/api-reference#ref/query_syntax">Search query syntax</a>
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used here
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> asyncSearch(String searchString, Callback callback) {
        return asyncSearch(searchString, callback, CMRequestOptions.NONE);
    }

    /**
     * Retrieve all the objects that match the given search
     * @param searchString the search string to use. For more information on syntax. See <a href="https://cloudmine.me/docs/api-reference#ref/query_syntax">Search query syntax</a>
     * @param callback the callback to pass the results into. It is recommended that {@link com.cloudmine.api.rest.callbacks.SimpleCMObjectResponseCallback} is used here
     * @param options options to apply to the call, such as a server function to pass the results of the call into, paging options, etc
     * @return a Future containing the {@link SimpleCMObjectResponse} containing the retrieved objects.
     */
    public Future<SimpleCMObjectResponse> asyncSearch(String searchString, Callback callback, CMRequestOptions options) {
        return executeAsyncCommand(addRequestOptions(createSearch(searchString), options),
                callback, simpleCMObjectResponseConstructor());
    }

    /**
     * Asynchronously insert the object. If it already exists in CloudMine, its contents will be replaced entirely
     * @param toCreate the object to save
     * @return a Future containing the {@link ObjectModificationResponse}
     * @throws JsonConversionException if unable to convert the SimpleCMObject to json. This ordinarily should not occur
     */
    public Future<ObjectModificationResponse> asyncInsert(SimpleCMObject toCreate) throws JsonConversionException {
        return asyncInsert(toCreate, Callback.DO_NOTHING);
    }

    /**
     * Asynchronously insert the object. If it already exists in CloudMine, its contents will be replaced entirely
     * @param toCreate the object to save
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @return a Future containing the {@link ObjectModificationResponse}
     * @throws JsonConversionException if unable to convert the SimpleCMObject to json. This ordinarily should not occur
     */
    public Future<ObjectModificationResponse> asyncInsert(SimpleCMObject toCreate, Callback callback) throws JsonConversionException {
        return asyncInsert(toCreate, callback, CMRequestOptions.NONE);
    }

    /**
     * Asynchronously insert the object. If it already exists in CloudMine, its contents will be replaced entirely
     * @param toCreate the object to save
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     * @return a Future containing the {@link ObjectModificationResponse}
     * @throws JsonConversionException if unable to convert the SimpleCMObject to json. This ordinarily should not occur
     */
    public Future<ObjectModificationResponse> asyncInsert(SimpleCMObject toCreate, Callback callback, CMRequestOptions options) throws JsonConversionException {
        return executeAsyncCommand(
                addRequestOptions(createPut(toCreate.asJson()), options),
                callback, objectModificationResponseConstructor());
    }

    /**
     * Asynchronously insert all of the objects. If any already exists in CloudMine, its contents will be replaced entirely
     * @param toCreate the objects to save
     * @return a Future containing the {@link ObjectModificationResponse}
     * @throws JsonConversionException if unable to convert the SimpleCMObject to json. This ordinarily should not occur
     */
    public Future<ObjectModificationResponse> asyncInsert(Collection<SimpleCMObject> toCreate) throws JsonConversionException {
        return asyncInsert(toCreate, Callback.DO_NOTHING);
    }

    /**
     * Asynchronously insert all of the objects. If any already exists in CloudMine, its contents will be replaced entirely
     * @param toCreate the objects to save
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @return a Future containing the {@link ObjectModificationResponse}
     * @throws JsonConversionException if unable to convert the SimpleCMObject to json. This ordinarily should not occur
     */
    public Future<ObjectModificationResponse> asyncInsert(Collection<SimpleCMObject> toCreate, Callback callback) throws JsonConversionException {
        return asyncInsert(toCreate, callback, CMRequestOptions.NONE);
    }

    /**
     * Asynchronously insert all of the objects. If any already exists in CloudMine, its contents will be replaced entirely
     * @param toCreate the objects to save
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @param options options to apply to the call, such as a server function to pass the results of the call into
     * @return a Future containing the {@link ObjectModificationResponse}
     * @throws JsonConversionException if unable to convert the SimpleCMObject to json. This ordinarily should not occur
     */
    public Future<ObjectModificationResponse> asyncInsert(Collection<SimpleCMObject> toCreate, Callback callback, CMRequestOptions options) throws JsonConversionException {
        List<Json> jsons = new ArrayList<Json>(toCreate.size());
        for(SimpleCMObject object : toCreate) {
            jsons.add(new JsonString(object.asKeyedObject()));
        }
        String jsonStringsCollection = JsonUtilities.jsonCollection(
                jsons.toArray(new Json[jsons.size()])
        ).asJson();
        return executeAsyncCommand(addRequestOptions(createPut(jsonStringsCollection), options),
                callback, objectModificationResponseConstructor());
    }

    /**
     * Asynchronously update the object. If it already exists in CloudMine, its contents will be merged
     * @param toUpdate the object to update
     * @return a Future containing the {@link ObjectModificationResponse}
     * @throws JsonConversionException if unable to convert the SimpleCMObject to json. This ordinarily should not occur
     */
    public Future<ObjectModificationResponse> asyncUpdate(SimpleCMObject toUpdate) throws JsonConversionException {
        return asyncUpdate(toUpdate, Callback.DO_NOTHING);
    }

    /**
     * Asynchronously update the object. If it already exists in CloudMine, its contents will be merged
     * @param toUpdate the object to update
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @return a Future containing the {@link ObjectModificationResponse}
     * @throws JsonConversionException if unable to convert the SimpleCMObject to json. This ordinarily should not occur
     */
    public Future<ObjectModificationResponse> asyncUpdate(SimpleCMObject toUpdate, Callback callback) throws JsonConversionException {
        return executeAsyncCommand(createJsonPost(toUpdate.asJson()), callback, objectModificationResponseConstructor());
    }

    /**
     * Asynchronously update all of the objects. If any already exists in CloudMine, its contents will be merged
     * @param objects the objects to update
     * @return a Future containing the {@link ObjectModificationResponse}
     * @throws JsonConversionException if unable to convert the SimpleCMObject to json. This ordinarily should not occur
     */
    public Future<ObjectModificationResponse> asyncUpdate(Collection<SimpleCMObject> objects) throws JsonConversionException {
        return asyncUpdate(objects, Callback.DO_NOTHING);
    }

    /**
     * Asynchronously update all of the objects. If any already exists in CloudMine, its contents will be merged
     * @param objects the objects to update
     * @param callback a Callback that expects an ObjectModificationResponse or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback} is passed in for this
     * @return a Future containing the {@link ObjectModificationResponse}
     * @throws JsonConversionException if unable to convert the SimpleCMObject to json. This ordinarily should not occur
     */
    public Future<ObjectModificationResponse> asyncUpdate(Collection<SimpleCMObject> objects, Callback callback) throws JsonConversionException {
        String[] jsonStrings = new String[objects.size()];
        int i = 0;
        for(SimpleCMObject cmObject : objects) {
            jsonStrings[i] = cmObject.asKeyedObject();
            i++;
        }
        String json = JsonUtilities.jsonCollection(jsonStrings).asJson();
        return executeAsyncCommand(createJsonPost(json), callback, objectModificationResponseConstructor());
    }

    /**
     * Create a new user
     * @param user the user to create
     * @return a CMResponse that indicates success or failure
     */
    public Future<CMResponse> asyncCreateUser(CMUser user)  {
        return executeAsyncCommand(createPut(user));
    }

    /**
     * Create a new user
     * @param user the user to create
     * @param callback a Callback that expects a CMResponse. It is recommended that a {@link com.cloudmine.api.rest.callbacks.CMResponseCallback} is given here
     * @return a CMResponse that indicates success or failure
     */
    public Future<CMResponse> asyncCreateUser(CMUser user, Callback callback) {
        return executeAsyncCommand(createPut(user), callback, cmResponseConstructor());
    }

    /**
     * Change the given user's password to newPassword
     * @param user the user whose password is to be changed
     * @param newPassword the new password
     * @return a CMResponse that indicates success or failure
     */
    public Future<CMResponse> asyncChangePassword(CMUser user, String newPassword) {
        return asyncChangePassword(user, newPassword, Callback.DO_NOTHING);
    }

    /**
     * Change the given user's password to newPassword
     * @param user the user whose password is to be changed
     * @param newPassword the new password
     * @param callback a Callback that expects a CMResponse. It is recommended that a {@link com.cloudmine.api.rest.callbacks.CMResponseCallback} is given here
     * @return a CMResponse that indicates success or failure
     */
    public Future<CMResponse> asyncChangePassword(CMUser user, String newPassword, Callback callback) {
        return executeAsyncCommand(createChangePassword(user, newPassword), callback);
    }

    /**
     * Asynchronously Request that the user with the given e-mail address's password is reset. This will generate a password reset e-mail that will be sent to the user
     * @param email the e-mail address of the user
     * @return a Future containing the {@link CMResponse} generated by this request
     */
    public Future<CMResponse> asyncResetPasswordRequest(String email) {
        return asyncResetPasswordRequest(email, Callback.DO_NOTHING);
    }

    /**
     * Asynchronously Request that the user with the given e-mail address's password is reset. This will generate a password reset e-mail that will be sent to the user
     * @param email the e-mail address of the user
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link CMResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.CMResponseCallback} is passed in
     * @return a Future containing the {@link CMResponse} generated by this request
     */
    public Future<CMResponse> asyncResetPasswordRequest(String email, Callback callback) {
        return executeAsyncCommand(createResetPassword(email), callback);
    }

    /**
     * Asynchronously confirm that a users password should be reset. Requires the token sent to the user's email address
     * @param token from the e-mail sent to the user
     * @param newPassword the new password
     * @return a Future containing the {@link CMResponse} generated by this request
     */
    public Future<CMResponse> asyncResetPasswordConfirmation(String token, String newPassword) {
        return asyncResetPasswordConfirmation(token, newPassword, Callback.DO_NOTHING);
    }

    /**
     * Asynchronously confirm that a users password should be reset. Requires the token sent to the user's email address
     * @param token from the e-mail sent to the user
     * @param newPassword the new password
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link CMResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.CMResponseCallback} is passed in
     * @return a Future containing the {@link CMResponse} generated by this request
     */
    public Future<CMResponse> asyncResetPasswordConfirmation(String token, String newPassword, Callback callback) {
        return executeAsyncCommand(createResetPasswordConfirmation(token, newPassword), callback);
    }

    /**
     * Asynchronously log in this user
     * @param user the user to log in
     * @return A Future containing the {@link LoginResponse} which will include the CMSessionToken that authenticates this user and provides access to the user level store
     */
    public Future<LoginResponse> asyncLogin(CMUser user) {
        return asyncLogin(user, Callback.DO_NOTHING);
    }

    /**
     * Asynchronously log in this user
     * @param user the user to log in
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects an {@link LoginResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.LoginResponseCallback} is passed in
     * @return A Future containing the {@link LoginResponse} which will include the CMSessionToken that authenticates this user and provides access to the user level store
     */
    public Future<LoginResponse> asyncLogin(CMUser user, Callback callback) {
        return executeAsyncCommand(createLoginPost(user), callback, logInResponseConstructor());
    }

    /**
     * Invalidate the given session token. Note that if other session tokens exist for this user, they will still be valid
     * @param token the token to invalidate
     * @return a Future containing the CMResponse containing success and failure information
     */
    public Future<CMResponse> asyncLogout(CMSessionToken token) {
        return asyncLogout(token, Callback.DO_NOTHING);
    }

    /**
     * Invalidate the given session token. Note that if other session tokens exist for this user, they will still be valid
     * @param token the token to invalidate
     * @param callback a {@link Callback} that expects a {@link CMResponse}. It is recommended that a {@link com.cloudmine.api.rest.callbacks.CMResponseCallback} is used here
     * @return a Future containing the CMResponse containing success and failure information
     */
    public Future<CMResponse> asyncLogout(CMSessionToken token, Callback callback) {
        return executeAsyncCommand(createLogoutPost(token), callback, cmResponseConstructor());
    }

    /**
     * Make a blocking call to load the object associated with the given objectId
     * @param objectId of the object to load
     * @return a SimpleCMObjectResponse containing success or failure, and the loaded object if it exists and the call was a success
     * @throws NetworkException if unable to perform the request
     */
    public SimpleCMObjectResponse loadObject(String objectId) throws NetworkException {
        return loadObjects(Collections.singletonList(objectId));
    }

    /**
     * Make a blocking call to load all of the objects associated with the given objectIds
     * @param objectIds of the objects to load
     * @return a SimpleCMObjectResponse containing success or failure, and the loaded objects if they exist and the call was a success
     * @throws NetworkException if unable to perform the request
     */
    public SimpleCMObjectResponse loadObjects(Collection<String> objectIds) throws NetworkException{
        return executeCommand(createGetObjects(objectIds), simpleCMObjectResponseConstructor());
    }

    /**
     * Make a blocking call to load all of the objects
     * @return a SimpleCMObjectResponse containing success or failure, and the loaded objects if they exist and the call was a success
     * @throws NetworkException if unable to perform the request
     */
    public SimpleCMObjectResponse loadAllObjects() throws NetworkException {
        return executeCommand(createGet(), simpleCMObjectResponseConstructor());
    }

    /**
     * Make a blocking call to load the specified file
     * @param fileName the name of the file to load
     * @return a FileLoadResponse that contains the loaded file, if the call was a success
     * @throws NetworkException if unable to perform the network call
     * @throws CreationException if unable to create the CMFile
     */
    public FileLoadResponse loadFile(String fileName) throws NetworkException, CreationException {
        try {
            HttpResponse response = httpClient.execute(createGetFile(fileName));
            return new FileLoadResponse(response, fileName);
        } catch (IOException e) {
            LOG.error("IOException getting file", e);
            throw new CreationException("Couldn't get file because of IOException", e);
        }
    }

    /**
     * Make a blocking call to search for CloudMine objects.
     * @param searchString the search string to use. For more information on syntax. See <a href="https://cloudmine.me/docs/api-reference#ref/query_syntax">Search query syntax</a>
     * @return  the {@link SimpleCMObjectResponse} containing the retrieved objects.
     * @throws NetworkException if unable to perform the network call
     */
    public SimpleCMObjectResponse loadSearch(String searchString) throws NetworkException {
        HttpGet get = createSearch(searchString);
        return executeCommand(get, simpleCMObjectResponseConstructor());
    }

    /**
     * Make a blocking call to directly insert JSON into CloudMine
     * @param json a valid JSON representation of a CloudMine object
     * @return the ObjectModificationResponse containing success and error values
     * @throws NetworkException if unable to perform the network call
     */
    public ObjectModificationResponse insert(String json) throws NetworkException {
        HttpPut put = createPut(json);
        return executeCommand(put, objectModificationResponseConstructor());
    }

    /**
     * Make a blocking call to directly update JSON in CloudMine. If the object already exists, its values will be updated; otherwise it will be inserted
     * @param json a valid JSON representation of a CloudMine object
     * @return the ObjectModificationResponse containing success and error values
     * @throws NetworkException if unable to perform the network call
     */
    public ObjectModificationResponse update(String json) throws NetworkException {
        HttpPost post = createJsonPost(json);
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
    public CMResponse changePassword(CMUser user, String newPassword) throws NetworkException {
        return executeCommand(createChangePassword(user, newPassword));
    }


    /**
     * Make a blocking call to create this user
     * @return  the {@link LoginResponse} which will include the CMSessionToken that authenticates this user and provides access to the user level store
     * @throws NetworkException if unable to perform the network call
     * @throws JsonConversionException if unable to convert this user to JSON. This should never happen
     */
    public CMResponse insert(CMUser user) throws NetworkException, JsonConversionException {
        return executeCommand(createPut(user));
    }

    /**
     * Make a blocking call to log in this user
     * @param user to log in
     * @return a LoginResponse that will contain the CMSessionToken used to validate user requests
     * @throws NetworkException if unable to perform the network call
     */
    public LoginResponse login(CMUser user) throws NetworkException {
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

    private Future<CMResponse> executeAsyncCommand(HttpUriRequest message) {
        return executeAsyncCommand(message, Callback.DO_NOTHING, cmResponseConstructor());
    }

    private Future<CMResponse> executeAsyncCommand(HttpUriRequest message, Callback callback) {
        return executeAsyncCommand(message, callback, cmResponseConstructor());
    }

    private <T> Future<T> executeAsyncCommand(HttpUriRequest message, Callback callback, ResponseConstructor<T> constructor) {
        return asyncHttpClient.executeCommand(message, callback, constructor);
    }

    private CMResponse executeCommand(HttpUriRequest message) throws NetworkException {
        return executeCommand(message, cmResponseConstructor());
    }

    private <T extends CMResponse> T executeCommand(HttpUriRequest message, ResponseConstructor<T> constructor) throws NetworkException{
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
    private HttpGet createSearch(String search) {
        HttpGet get = new HttpGet(baseUrl.search(search).asUrlString());
        addCloudMineHeader(get);
        return get;
    }

    private HttpDelete createDeleteAll() {
        HttpDelete delete = new HttpDelete(baseUrl.deleteAll().asUrlString());
        addCloudMineHeader(delete);
        return delete;
    }

    private HttpDelete createDelete(String key) {
        HttpDelete delete = new HttpDelete(baseUrl.delete(key).asUrlString());
        addCloudMineHeader(delete);
        return delete;
    }

    private HttpDelete createDelete(Collection<String> keys) {
        HttpDelete delete = new HttpDelete(baseUrl.delete(keys).asUrlString());

        addCloudMineHeader(delete);
        return delete;
    }

    private HttpPut createPut(String json) {
        HttpPut put = new HttpPut(baseUrl.text().asUrlString());
        addCloudMineHeader(put);
        addJson(put, json);
        return put;
    }

    private HttpPut createPut(CMUser user) throws JsonConversionException {
        HttpPut put = new HttpPut(baseUrl.account().create().asUrlString());
        addCloudMineHeader(put);
        addJson(put, user.asJson());
        return put;
    }

    private HttpPut createPut(CMFile file) {
        HttpPut put = new HttpPut(baseUrl.binary(file.getFileName()).asUrlString());
        addCloudMineHeader(put);
        put.setEntity(new ByteArrayEntity(file.getFileContents()));
        put.addHeader("Content-Type", file.getContentType());
        return put;
    }

    private HttpPost createJsonPost(String json) {
        HttpPost post = createPost(baseUrl.text().asUrlString());
        addJson(post, json);
        return post;
    }

    private HttpPost createLoginPost(CMUser user) {
        HttpPost post = createPost(baseUrl.account().login().asUrlString());
        addAuthorizationHeader(user, post);
        return post;
    }

    private HttpPost createLogoutPost(CMSessionToken sessionToken) {
        HttpPost post = createPost(baseUrl.account().logout().asUrlString());
        post.addHeader("X-CloudMine-SessionToken", sessionToken.getSessionToken());
        return post;
    }

    private HttpPost createPost(String url){
        HttpPost post = new HttpPost(url);
        addCloudMineHeader(post);
        return post;
    }

    private HttpGet createGet() {
        HttpGet get = new HttpGet(baseUrl.text().asUrlString());
        addCloudMineHeader(get);
        return get;
    }

    private HttpGet createGetFile(String key) {
        HttpGet get = new HttpGet(baseUrl.binary(key).asUrlString());
        addCloudMineHeader(get);
        return get;
    }

    private HttpGet createGetObjects(Collection<String> keys) {
        HttpGet get = new HttpGet(baseUrl.text().objectIds(keys).asUrlString());
        addCloudMineHeader(get);
        return get;
    }

    private HttpPost createResetPasswordConfirmation(String token, String newPassword) {
        HttpPost post = new HttpPost(baseUrl.account().password().reset().addAction(token).asUrlString());
        addCloudMineHeader(post);
        try {
            addJson(post, JsonUtilities.jsonCollection(
                    JsonUtilities.createJsonProperty(PASSWORD_KEY, newPassword)
            ));
        } catch (JsonConversionException e) {
            LOG.error("Unable to create jsoncollection", e); //this should not happen ever so we swallow
        }
        return post;
    }

    private HttpPost createResetPassword(String email) {
        HttpPost post = new HttpPost(baseUrl.account().password().reset().asUrlString());
        addCloudMineHeader(post);
        try {
            addJson(post, JsonUtilities.jsonCollection(
                    JsonUtilities.createJsonProperty(EMAIL_KEY, email)
            ));
        } catch (JsonConversionException e) {
            LOG.error("Unable to create json collection from email key", e); //this should never happen
        }
        return post;
    }

    private HttpPost createChangePassword(CMUser user, String newPassword) {
        HttpPost post = new HttpPost(baseUrl.account().password().change().asUrlString());
        addCloudMineHeader(post);
        addAuthorizationHeader(user, post);
        try {
            addJson(post, JsonUtilities.jsonCollection(
                    JsonUtilities.createJsonProperty(PASSWORD_KEY, newPassword)));
        } catch (JsonConversionException e) {
            LOG.error("Unable to create json collection from property", e); //this should never happen
        }
        return post;
    }

    private void addJson(HttpEntityEnclosingRequestBase message, String json) {
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

    private void addJson(HttpEntityEnclosingRequestBase message, Json json) throws JsonConversionException {
        addJson(message, json.asJson());
    }

    private void addAuthorizationHeader(CMUser user, HttpEntityEnclosingRequestBase post) {
        post.addHeader(AUTHORIZATION_KEY, "Basic " + user.encode());
    }

    protected void addCloudMineHeader(AbstractHttpMessage message) {
        message.addHeader(CMApiCredentials.getCloudMineHeader());
        message.addHeader(new BasicHeader(AGENT_HEADER_KEY, getCloudMineAgent()));
    }

    protected HttpRequestBase addRequestOptions(HttpRequestBase message, CMRequestOptions options) {
        if(options != CMRequestOptions.NONE) {
            String url = message.getURI().toASCIIString();
            url = url + options.asUrlString();
            try {
                message.setURI(new URI(url));
            } catch (URISyntaxException e) {
                LOG.error("Unable to set the CMRequestionOptions for url: " + message.getURI().toASCIIString() + ", tried to add: " + options.asUrlString());
            }
        }
        return message;
    }

    protected String getCloudMineAgent() {
        return CLOUD_MINE_AGENT;
    }
    //**********************RESPONSE CONSTRUCTORS******************************
    protected ResponseConstructor<FileCreationResponse> fileCreationResponseConstructor() {
        return FileCreationResponse.CONSTRUCTOR;
    }

    protected ResponseConstructor<ObjectModificationResponse> objectModificationResponseConstructor() {
        return ObjectModificationResponse.CONSTRUCTOR;
    }

    protected ResponseConstructor<CMResponse> cmResponseConstructor() {
        return CMResponse.CONSTRUCTOR;
    }

    protected ResponseConstructor<FileLoadResponse> fileLoadResponseResponseConstructor(String key) {
        return FileLoadResponse.constructor(key);
    }

    protected ResponseConstructor<CMFile> cmFileConstructor(String key) {
        return CMFile.constructor(key);
    }

    protected ResponseConstructor<LoginResponse> logInResponseConstructor() {
        return LoginResponse.CONSTRUCTOR;
    }

    private ResponseConstructor<SimpleCMObjectResponse> simpleCMObjectResponseConstructor() {
        return SimpleCMObjectResponse.CONSTRUCTOR;
    }
}

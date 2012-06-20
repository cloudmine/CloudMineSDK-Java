package com.cloudmine.api.rest;

import com.cloudmine.api.*;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.exceptions.JsonConversionException;
import com.cloudmine.api.rest.callbacks.WebServiceCallback;
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
     * If the entity response is not fully consumed, the connection will not be released
     * @param response
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
     * userWebService, unless you pass userWebService a CMSessionToken
     * @param token the token retrieved from a LoginResponse
     * @return the UserCMWebService that is created from this request.
     */
    public synchronized UserCMWebService setLoggedInUser(CMSessionToken token) throws CreationException {
        loggedInSessionToken = token;
        return getUserWebService(token);
    }

    public synchronized UserCMWebService getUserWebService() throws CreationException {
        if(loggedInSessionToken == null) {
            throw new CreationException("Cannot request a user web service until setLoggedInUser has been called");
        }
        return getUserWebService(loggedInSessionToken);
    }

    public ObjectModificationResponse deleteAll() throws CreationException {
        return executeCommand(createDeleteAll(), objectModificationResponseConstructor());
    }


    public ObjectModificationResponse delete(Collection<String> keys) throws CreationException {
        return executeCommand(createDelete(keys), objectModificationResponseConstructor());
    }

    public ObjectModificationResponse delete(String key) throws CreationException {
        return executeCommand(createDelete(key), objectModificationResponseConstructor());
    }

    public Future<SimpleCMObjectResponse> asyncLoadObjectsOfClass(String klass) {
        return asyncLoadObjectsOfClass(klass, WebServiceCallback.DO_NOTHING);
    }

    public Future<SimpleCMObjectResponse> asyncLoadObjectsOfClass(String klass, WebServiceCallback callback) {
        return asyncLoadObjectsOfClass(klass, callback, CMRequestOptions.NONE);
    }

    public Future<SimpleCMObjectResponse> asyncLoadObjectsOfClass(String klass, WebServiceCallback callback, CMRequestOptions options) {
        HttpGet search = createSearch("[" + JsonUtilities.CLASS_KEY + "=" + JsonUtilities.addQuotes(klass) + "]");
        return executeAsyncCommand(addRequestOptions(search, options),
                callback, simpleCMObjectResponseConstructor());
    }

    public Future<ObjectModificationResponse> asyncDeleteObject(SimpleCMObject object) {
        return asyncDeleteObject(object, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncDeleteObject(SimpleCMObject object, WebServiceCallback callback) {
        return asyncDeleteObject(object, callback, CMRequestOptions.NONE);
    }

    public Future<ObjectModificationResponse> asyncDeleteObject(SimpleCMObject object, WebServiceCallback callback, CMRequestOptions options) {
        return asyncDeleteObjects(Collections.singletonList(object), callback, options);
    }

    public Future<ObjectModificationResponse> asyncDeleteObjects(Collection<SimpleCMObject> objects) {
        return asyncDeleteObjects(objects, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncDeleteObjects(Collection<SimpleCMObject> objects, WebServiceCallback callback) {
        return asyncDeleteObjects(objects, callback, CMRequestOptions.NONE);
    }

    public Future<ObjectModificationResponse> asyncDeleteObjects(Collection<SimpleCMObject> objects, WebServiceCallback callback, CMRequestOptions options) {
        int size = objects.size();
        Collection<String> keys = new ArrayList<String>(size);
        for(SimpleCMObject object : objects) {
            keys.add(object.getObjectId());
        }
        return asyncDelete(keys, callback, options);
    }

    public Future<ObjectModificationResponse> asyncDelete(String key) {
        return asyncDelete(key, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncDelete(String key, WebServiceCallback callback) {
        return asyncDelete(key, callback, CMRequestOptions.NONE);
    }

    public Future<ObjectModificationResponse> asyncDelete(String key, WebServiceCallback callback, CMRequestOptions options) {
        return asyncDelete(Collections.singletonList(key), callback, options);
    }

    public Future<ObjectModificationResponse> asyncDelete(Collection<String> keys) {
        return asyncDelete(keys, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncDelete(Collection<String> keys, WebServiceCallback callback) {
        return asyncDelete(keys, callback, CMRequestOptions.NONE);
    }

    public Future<ObjectModificationResponse> asyncDelete(Collection<String> keys, WebServiceCallback callback, CMRequestOptions options) {
        return executeAsyncCommand(addRequestOptions(createDelete(keys), options),
                callback, objectModificationResponseConstructor());
    }

    /**
     * This will delete ALL the objects associated with this API key. Be careful...
     * @param callback
     * @return
     */
    public Future<ObjectModificationResponse> asyncDeleteAll(WebServiceCallback callback) {
        return executeAsyncCommand(createDeleteAll(), callback, objectModificationResponseConstructor());
    }

    public Future<ObjectModificationResponse> asyncDeleteAll() {
        return asyncDeleteAll(WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncDeleteFile(CMFile file) {
        return asyncDeleteFile(file, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncDeleteFile(CMFile file, WebServiceCallback callback) {
        return asyncDelete(file.getFileKey(), callback);
    }

    public Future<ObjectModificationResponse> asyncDeleteFiles(Collection<CMFile> files) {
        return asyncDeleteFiles(files, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncDeleteFiles(Collection<CMFile> files, WebServiceCallback callback) {
        Collection<String> keys = new ArrayList<String>(files.size());
        for(CMFile file : files) {
            keys.add(file.getFileKey());
        }
        return asyncDelete(keys, callback);
    }

    public Future<ObjectModificationResponse> asyncDeleteFile(String fileName) {
        return asyncDeleteFile(fileName, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncDeleteFile(String fileName, WebServiceCallback callback) {
        return asyncDeleteFile(fileName, callback, CMRequestOptions.NONE);
    }

    public Future<ObjectModificationResponse> asyncDeleteFile(String fileName, WebServiceCallback callback, CMRequestOptions options) {
        return asyncDelete(fileName, callback, options);
    }

    public Future<FileCreationResponse> asyncUpload(CMFile file) {
        return asyncUpload(file, WebServiceCallback.DO_NOTHING);
    }

    public Future<FileCreationResponse> asyncUpload(CMFile file, WebServiceCallback callback) {
        return executeAsyncCommand(createPut(file), callback, fileCreationResponseConstructor());
    }

    public Future<CMFile> asyncLoadFile(String key) {
        return asyncLoadFile(key, WebServiceCallback.DO_NOTHING);
    }

    public Future<CMFile> asyncLoadFile(String key, WebServiceCallback callback) {
        return asyncLoadFile(key, callback, CMRequestOptions.NONE);
    }

    public Future<CMFile> asyncLoadFile(String key, WebServiceCallback callback, CMRequestOptions options) {
        return executeAsyncCommand(addRequestOptions(createGetFile(key), options),
                callback, cmFileConstructor(key));
    }

    public Future<SimpleCMObjectResponse> asyncLoadObjects() {
        return asyncLoadObjects(WebServiceCallback.DO_NOTHING);
    }

    public Future<SimpleCMObjectResponse> asyncLoadObjects(WebServiceCallback callback) {
        return asyncLoadObjects(callback, CMRequestOptions.NONE);
    }

    public Future<SimpleCMObjectResponse> asyncLoadObjects(WebServiceCallback callback, CMRequestOptions options) {
        return asyncLoadObjects(Collections.<String>emptyList(), callback, options);
    }

    public Future<SimpleCMObjectResponse> asyncLoadObject(String key) {
        return asyncLoadObject(key, WebServiceCallback.DO_NOTHING);
    }

    public Future<SimpleCMObjectResponse> asyncLoadObject(String key, WebServiceCallback callback) {
        return asyncLoadObjects(Collections.<String>singleton(key), callback);
    }

    public Future<SimpleCMObjectResponse> asyncLoadObjects(Collection<String> keys) {
        return asyncLoadObjects(keys, WebServiceCallback.DO_NOTHING);
    }

    public Future<SimpleCMObjectResponse> asyncLoadObjects(Collection<String> keys, WebServiceCallback callback) {
        return asyncLoadObjects(keys, callback, CMRequestOptions.NONE);
    }

    public Future<SimpleCMObjectResponse> asyncLoadObjects(Collection<String> keys, WebServiceCallback callback, CMRequestOptions options) {
        return executeAsyncCommand(addRequestOptions(createGetObjects(keys), options),
                callback, simpleCMObjectResponseConstructor());
    }


    public Future<SimpleCMObjectResponse> asyncSearch(String searchString) {
        return asyncSearch(searchString, WebServiceCallback.DO_NOTHING);
    }

    public Future<SimpleCMObjectResponse> asyncSearch(String searchString, WebServiceCallback callback) {
        return asyncSearch(searchString, callback, CMRequestOptions.NONE);
    }

    public Future<SimpleCMObjectResponse> asyncSearch(String searchString, WebServiceCallback callback, CMRequestOptions options) {
        return executeAsyncCommand(addRequestOptions(createSearch(searchString), options),
                callback, simpleCMObjectResponseConstructor());
    }

    public Future<ObjectModificationResponse> asyncInsert(SimpleCMObject toCreate) throws JsonConversionException {
        return asyncInsert(toCreate, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncInsert(SimpleCMObject toCreate, WebServiceCallback callback) throws JsonConversionException {
        return asyncInsert(toCreate, callback, CMRequestOptions.NONE);
    }

    public Future<ObjectModificationResponse> asyncInsert(SimpleCMObject toCreate, WebServiceCallback callback, CMRequestOptions options) throws JsonConversionException {
        return executeAsyncCommand(
                addRequestOptions(createPut(toCreate.asJson()), options),
                callback, objectModificationResponseConstructor());
    }

    public Future<ObjectModificationResponse> asyncInsert(Collection<SimpleCMObject> toCreate) throws JsonConversionException {
        return asyncInsert(toCreate, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncInsert(Collection<SimpleCMObject> toCreate, WebServiceCallback callback) throws JsonConversionException {
        return asyncInsert(toCreate, callback, CMRequestOptions.NONE);
    }

    public Future<ObjectModificationResponse> asyncInsert(Collection<SimpleCMObject> toCreate, WebServiceCallback callback, CMRequestOptions options) throws JsonConversionException {
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

    public Future<ObjectModificationResponse> asyncUpdate(SimpleCMObject toUpdate) throws JsonConversionException {
        return asyncUpdate(toUpdate, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncUpdate(SimpleCMObject toUpdate, WebServiceCallback callback) throws JsonConversionException {
        return executeAsyncCommand(createJsonPost(toUpdate.asJson()), callback, objectModificationResponseConstructor());
    }

    public Future<ObjectModificationResponse> asyncUpdateAll(Collection<SimpleCMObject> objects) throws JsonConversionException {
        return asyncUpdate(objects, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncUpdate(Collection<SimpleCMObject> objects, WebServiceCallback callback) throws JsonConversionException {
        String[] jsonStrings = new String[objects.size()];
        int i = 0;
        for(SimpleCMObject cmObject : objects) {
            jsonStrings[i] = cmObject.asKeyedObject();
            i++;
        }
        String json = JsonUtilities.jsonCollection(jsonStrings).asJson();
        return executeAsyncCommand(createJsonPost(json), callback, objectModificationResponseConstructor());
    }

    public Future<CMResponse> asyncCreateUser(CMUser user) throws JsonConversionException {
        return executeAsyncCommand(createPut(user));
    }

    public Future<CMResponse> asyncCreateUser(CMUser user, WebServiceCallback callback) throws JsonConversionException {
        return executeAsyncCommand(createPut(user), callback, cmResponseConstructor());
    }

    public Future<CMResponse> asyncChangePassword(CMUser user, String newPassword) {
        return asyncChangePassword(user, newPassword, WebServiceCallback.DO_NOTHING);
    }

    public Future<CMResponse> asyncChangePassword(CMUser user, String newPassword, WebServiceCallback callback) {
        return executeAsyncCommand(createChangePassword(user, newPassword), callback);
    }

    public Future<CMResponse> asyncResetPasswordRequest(String email) {
        return asyncResetPasswordRequest(email, WebServiceCallback.DO_NOTHING);
    }

    public Future<CMResponse> asyncResetPasswordRequest(String email, WebServiceCallback callback) {
        return executeAsyncCommand(createResetPassword(email), callback);
    }

    public Future<CMResponse> asyncResetPasswordConfirmation(String token, String newPassword) {
        return asyncResetPasswordConfirmation(token, newPassword, WebServiceCallback.DO_NOTHING);
    }

    public Future<CMResponse> asyncResetPasswordConfirmation(String token, String newPassword, WebServiceCallback callback) {
        return executeAsyncCommand(createResetPasswordConfirmation(token, newPassword), callback);
    }

    public Future<LoginResponse> asyncLogin(CMUser user) {
        return asyncLogin(user, WebServiceCallback.DO_NOTHING);
    }

    public Future<LoginResponse> asyncLogin(CMUser user, WebServiceCallback callback) {
        return executeAsyncCommand(createLoginPost(user), callback, logInResponseConstructor());
    }

    public Future<CMResponse> asyncLogout(CMSessionToken token) {
        return asyncLogout(token, WebServiceCallback.DO_NOTHING);
    }

    public Future<CMResponse> asyncLogout(CMSessionToken token, WebServiceCallback callback) {
        return executeAsyncCommand(createLogoutPost(token), callback, cmResponseConstructor());
    }

    public SimpleCMObjectResponse loadObject(String objectId) throws CreationException {
        return loadObjects(Collections.singletonList(objectId));
    }

    /**
     *
     * @param objectIds
     * @return
     * @throws CreationException if unable to create the SimpleCMObjectResponse from the HttpResponse.
     */
    public SimpleCMObjectResponse loadObjects(Collection<String> objectIds) throws CreationException{
        return executeCommand(createGetObjects(objectIds), simpleCMObjectResponseConstructor());
    }

    public SimpleCMObjectResponse loadAllObjects() throws CreationException {
        return executeCommand(createGet(), simpleCMObjectResponseConstructor());
    }

    public CMFile loadFile(String key) throws CreationException {
        try {
            HttpResponse response = httpClient.execute(createGetFile(key));
            return CMFile.CMFile(response, key);
        } catch (IOException e) {
            LOG.error("IOException getting file", e);
            throw new CreationException("Couldn't get file because of IOException", e);
        }
    }

    public SimpleCMObjectResponse loadSearch(String searchString) throws CreationException {
        HttpGet get = createSearch(searchString);
        return executeCommand(get, simpleCMObjectResponseConstructor());
    }

    public ObjectModificationResponse insert(String json) throws CreationException {
        HttpPut put = createPut(json);
        return executeCommand(put, objectModificationResponseConstructor());
    }


    public ObjectModificationResponse update(String json) throws CreationException {
        HttpPost post = createJsonPost(json);
        return executeCommand(post, objectModificationResponseConstructor());
    }

    public FileCreationResponse insert(CMFile file) throws CreationException {
        return executeCommand(createPut(file), fileCreationResponseConstructor());
    }

    public CMResponse changePassword(CMUser user, String newPassword) throws CreationException {
        return executeCommand(createChangePassword(user, newPassword));
    }


    public CMResponse insert(CMUser user) throws CreationException, JsonConversionException {
        return executeCommand(createPut(user));
    }

    public LoginResponse login(CMUser user) throws CreationException {
        return executeCommand(createLoginPost(user), logInResponseConstructor());
    }

    public CMResponse logout(CMSessionToken sessionToken) throws CreationException {
        return executeCommand(createLogoutPost(sessionToken));
    }

    private Future<CMResponse> executeAsyncCommand(HttpUriRequest message) {
        return executeAsyncCommand(message, WebServiceCallback.DO_NOTHING, cmResponseConstructor());
    }

    private Future<CMResponse> executeAsyncCommand(HttpUriRequest message, WebServiceCallback callback) {
        return executeAsyncCommand(message, callback, cmResponseConstructor());
    }

    private <T> Future<T> executeAsyncCommand(HttpUriRequest message, WebServiceCallback callback, ResponseConstructor<T> constructor) {
        return asyncHttpClient.executeCommand(message, callback, constructor);
    }

    private CMResponse executeCommand(HttpUriRequest message) throws CreationException {
        return executeCommand(message, cmResponseConstructor());
    }

    private <T extends CMResponse> T executeCommand(HttpUriRequest message, ResponseConstructor<T> constructor) throws CreationException{
        HttpResponse response = null;
        try {
            response = httpClient.execute(message);
            return constructor.construct(response);
        }
        catch (IOException e) {
            LOG.error("Error executing command: " + message.getURI(), e);
            throw new CreationException("Couldn't execute command, IOException: ", e);
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
        HttpPut put = new HttpPut(baseUrl.binary(file.getFileKey()).asUrlString());
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

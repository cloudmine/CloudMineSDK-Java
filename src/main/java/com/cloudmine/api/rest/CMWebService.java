package com.cloudmine.api.rest;

import com.cloudmine.api.*;
import com.cloudmine.api.exceptions.CreationException;
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
import java.util.*;
import java.util.concurrent.Future;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/16/12, 2:34 PM
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
    private UserCMWebService userWebService;

    public static CMWebService service() {
        return AndroidCMWebService.service();
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

    public UserCMWebService userWebService(CMUserToken token) {
        return createUserCMWebService(token);
    }

    protected UserCMWebService createUserCMWebService(CMUserToken token) {
        return UserCMWebService.UserCMWebService(baseUrl.user(), token, asyncHttpClient);
    }

    /**
     * This will set the default UserCMWebService and return it.
     * @param token
     * @return
     */
    public UserCMWebService setLoggedInUser(CMUserToken token) {
        userWebService = userWebService(token);
        return userWebService;
    }

    public UserCMWebService userWebService() {
        if(userWebService == null) {
            throw new CreationException("Cannot request a user web service until setLoggedInUser has been called");
        }
        return userWebService;
    }

    public ObjectModificationResponse deleteAll() {
        return executeCommand(createDeleteAll(), objectModificationResponseConstructor());
    }


    public ObjectModificationResponse delete(Collection<String> keys) {
        return executeCommand(createDelete(keys), objectModificationResponseConstructor());
    }

    public ObjectModificationResponse delete(String key) {
        return executeCommand(createDelete(key), objectModificationResponseConstructor());
    }

    public Future<SimpleCMObjectResponse> allObjectsOfClass(String klass) {
        return allObjectsOfClass(klass, WebServiceCallback.DO_NOTHING);
    }

    public Future<SimpleCMObjectResponse> allObjectsOfClass(String klass, WebServiceCallback callback) {
        HttpGet search = createSearch("[" + JsonUtilities.CLASS_KEY + "=" + JsonUtilities.addQuotes(klass) + "]");
        return executeAsyncCommand(search, callback, simpleCMObjectResponseConstructor());
    }

    public Future<ObjectModificationResponse> asyncDeleteObject(SimpleCMObject object) {
        return asyncDeleteObject(object, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncDeleteObject(SimpleCMObject objects, WebServiceCallback callback) {
        return asyncDeleteObjects(Collections.singletonList(objects), callback);
    }

    public Future<ObjectModificationResponse> asyncDeleteObjects(Collection<SimpleCMObject> objects) {
        return asyncDeleteObjects(objects, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncDeleteObjects(Collection<SimpleCMObject> objects, WebServiceCallback callback) {
        int size = objects.size();
        Collection<String> keys = new ArrayList<String>(size);
        for(SimpleCMObject object : objects) {
            keys.add(object.key());
        }
        return asyncDelete(keys, callback);
    }

    public Future<ObjectModificationResponse> asyncDelete(String key) {
        return asyncDelete(key, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncDelete(String key, WebServiceCallback callback) {
        return asyncDelete(Collections.singletonList(key), callback);
    }

    public Future<ObjectModificationResponse> asyncDelete(Collection<String> keys) {
        return asyncDelete(keys, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncDelete(Collection<String> keys, WebServiceCallback callback) {
        return executeAsyncCommand(createDelete(keys), callback, objectModificationResponseConstructor());
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
        return asyncDelete(file.key(), callback);
    }

    public Future<ObjectModificationResponse> asyncDeleteFiles(Collection<CMFile> files) {
        return asyncDeleteFiles(files, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncDeleteFiles(Collection<CMFile> files, WebServiceCallback callback) {
        Collection<String> keys = new ArrayList<String>(files.size());
        for(CMFile file : files) {
            keys.add(file.key());
        }
        return asyncDelete(keys, callback);
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
        return executeAsyncCommand(createGetFile(key), callback, cmFileConstructor(key));
    }

    public Future<SimpleCMObjectResponse> asyncLoadObjects() {
        return asyncLoadObjects(WebServiceCallback.DO_NOTHING);
    }

    public Future<SimpleCMObjectResponse> asyncLoadObjects(WebServiceCallback callback) {
        return asyncLoadObjects(Collections.<String>emptyList(), callback);
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
        return executeAsyncCommand(createGetObjects(keys), callback, simpleCMObjectResponseConstructor());
    }

    public Future<SimpleCMObjectResponse> asyncSearch(String searchString) {
        return asyncSearch(searchString, WebServiceCallback.DO_NOTHING);
    }

    public Future<SimpleCMObjectResponse> asyncSearch(String searchString, WebServiceCallback callback) {
        return executeAsyncCommand(createSearch(searchString), callback, simpleCMObjectResponseConstructor());
    }

    public Future<ObjectModificationResponse> asyncInsert(SimpleCMObject toCreate) {
        return asyncInsert(toCreate, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncInsert(SimpleCMObject toCreate, WebServiceCallback callback) {
        return executeAsyncCommand(createPut(toCreate.asJson()), callback, objectModificationResponseConstructor());
    }

    public Future<ObjectModificationResponse> asyncInsert(Collection<SimpleCMObject> toCreate) {
        return asyncInsert(toCreate, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncInsert(Collection<SimpleCMObject> toCreate, WebServiceCallback callback) {
        List<Json> jsons = new ArrayList<Json>(toCreate.size());
        for(SimpleCMObject object : toCreate) {
            jsons.add(new JsonString(object.asKeyedObject()));
        }
        String jsonStringsCollection = JsonUtilities.jsonCollection(
                jsons.toArray(new Json[jsons.size()])
        ).asJson();
        return executeAsyncCommand(createPut(jsonStringsCollection), callback, objectModificationResponseConstructor());
    }

    public Future<ObjectModificationResponse> asyncUpdate(SimpleCMObject toUpdate) {
        return asyncUpdate(toUpdate, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncUpdate(SimpleCMObject toUpdate, WebServiceCallback callback) {
        return executeAsyncCommand(createJsonPost(toUpdate.asJson()), callback, objectModificationResponseConstructor());
    }

    public Future<ObjectModificationResponse> asyncUpdateAll(Collection<SimpleCMObject> objects) {
        return asyncUpdate(objects, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncUpdate(Collection<SimpleCMObject> objects, WebServiceCallback callback) {
        String[] jsonStrings = new String[objects.size()];
        int i = 0;
        for(SimpleCMObject cmObject : objects) {
            jsonStrings[i] = cmObject.asKeyedObject();
            i++;
        }
        String json = JsonUtilities.jsonCollection(jsonStrings).asJson();
        return executeAsyncCommand(createJsonPost(json), callback, objectModificationResponseConstructor());
    }

    public SimpleCMObjectResponse get(String key) {
        return get(Collections.singletonList(key));
    }

    public SimpleCMObjectResponse get(Collection<String> keys) {
        return executeCommand(createGetObjects(keys), simpleCMObjectResponseConstructor());
    }

    public SimpleCMObjectResponse get() {
        return executeCommand(createGet(), simpleCMObjectResponseConstructor());
    }

    public CMFile getFile(String key) {
        try {
            HttpResponse response = httpClient.execute(createGetFile(key));
            return CMFile.CMFile(response, key);
        } catch (IOException e) {
            //TODO handle this
        }
        return null;
    }

    public SimpleCMObjectResponse search(String searchString) {
        HttpGet get = createSearch(searchString);
        return executeCommand(get, simpleCMObjectResponseConstructor());
    }

    public ObjectModificationResponse set(String json) {
        HttpPut put = createPut(json);
        return executeCommand(put, objectModificationResponseConstructor());
    }

    public ObjectModificationResponse update(String json) {
        HttpPost post = createJsonPost(json);
        return executeCommand(post, objectModificationResponseConstructor());
    }

    public FileCreationResponse set(CMFile file) {
        return executeCommand(createPut(file), fileCreationResponseConstructor());
    }


    public Future<CMResponse> asyncCreateUser(CMUser user) {
        return executeAsyncCommand(createPut(user));
    }

    public Future<CMResponse> asyncCreateUser(CMUser user, WebServiceCallback callback) {
        return executeAsyncCommand(createPut(user), callback, cmResponseConstructor());
    }

    public CMResponse changePassword(CMUser user, String newPassword) {
        return executeCommand(createChangePassword(user, newPassword));
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

    public Future<LogInResponse> asyncLogin(CMUser user) {
        return asyncLogin(user, WebServiceCallback.DO_NOTHING);
    }

    public Future<LogInResponse> asyncLogin(CMUser user, WebServiceCallback callback) {
        return executeAsyncCommand(createLoginPost(user), callback, logInResponseConstructor());
    }

    public Future<CMResponse> asyncLogout(CMUserToken token) {
        return asyncLogout(token, WebServiceCallback.DO_NOTHING);
    }

    public Future<CMResponse> asyncLogout(CMUserToken token, WebServiceCallback callback) {
        return executeAsyncCommand(createLogoutPost(token), callback, cmResponseConstructor());
    }


    public CMResponse set(CMUser user) {
        return executeCommand(createPut(user));
    }

    public LogInResponse login(CMUser user) {
        return executeCommand(createLoginPost(user), logInResponseConstructor());
    }

    public CMResponse logout(CMUserToken sessionToken) {
        return executeCommand(createLogoutPost(sessionToken));
    }

    private CMResponse executeCommand(HttpUriRequest message) {
        return executeCommand(message, cmResponseConstructor());
    }

    private Future<CMResponse> executeAsyncCommand(HttpUriRequest message) {
        return executeAsyncCommand(message, WebServiceCallback.DO_NOTHING, cmResponseConstructor());
    }

    private Future<CMResponse> executeAsyncCommand(HttpUriRequest message, WebServiceCallback callback) {
        return executeAsyncCommand(message, callback, cmResponseConstructor());
    }

    private <T> Future<T> executeAsyncCommand(HttpUriRequest message, WebServiceCallback callback, ResponseConstructor<T> constructor) {
        return constructor.constructFuture(asyncHttpClient.executeCommand(message, callback, constructor));
    }

    private <T extends CMResponse> T executeCommand(HttpUriRequest message, ResponseConstructor<T> constructor) {
        HttpResponse response = null;
        try {
            response = httpClient.execute(message);
            return constructor.construct(response);
        }
        catch (IOException e) {
            LOG.error("Error executing command: " + message.getURI(), e);
        }
        finally {
            CMWebService.consumeEntityResponse(response);
        }
        return constructor.construct(null);
    }

    //**************************Http commands*****************************************
    private HttpGet createSearch(String search) {
        HttpGet get = new HttpGet(baseUrl.search(search).urlString());
        addCloudMineHeader(get);
        return get;
    }

    private HttpDelete createDeleteAll() {
        HttpDelete delete = new HttpDelete(baseUrl.deleteAll().urlString());
        addCloudMineHeader(delete);
        return delete;
    }

    private HttpDelete createDelete(String key) {
        HttpDelete delete = new HttpDelete(baseUrl.delete(key).urlString());
        addCloudMineHeader(delete);
        return delete;
    }

    private HttpDelete createDelete(Collection<String> keys) {
        HttpDelete delete = new HttpDelete(baseUrl.delete(keys).urlString());
        addCloudMineHeader(delete);
        return delete;
    }

    private HttpPut createPut(String json) {
        HttpPut put = new HttpPut(baseUrl.text().urlString());
        addCloudMineHeader(put);
        addJson(put, json);
        return put;
    }

    private HttpPut createPut(CMUser user) {
        HttpPut put = new HttpPut(baseUrl.account().create().urlString());
        addCloudMineHeader(put);
        addJson(put, user.asJson());
        return put;
    }

    private HttpPut createPut(CMFile file) {
        HttpPut put = new HttpPut(baseUrl.binary(file.key()).urlString());
        addCloudMineHeader(put);
        put.setEntity(new ByteArrayEntity(file.fileContents()));
        put.addHeader("Content-Type", file.contentType());
        return put;
    }

    private HttpPost createJsonPost(String json) {
        HttpPost post = createPost(baseUrl.text().urlString());
        addJson(post, json);
        return post;
    }

    private HttpPost createLoginPost(CMUser user) {
        HttpPost post = createPost(baseUrl.account().login().urlString());
        addAuthorizationHeader(user, post);
        return post;
    }

    private HttpPost createLogoutPost(CMUserToken sessionToken) {
        HttpPost post = createPost(baseUrl.account().logout().urlString());
        post.addHeader("X-CloudMine-SessionToken", sessionToken.sessionToken());
        return post;
    }

    private HttpPost createPost(String url){
        HttpPost post = new HttpPost(url);
        addCloudMineHeader(post);
        return post;
    }

    private HttpGet createGet() {
        HttpGet get = new HttpGet(baseUrl.text().urlString());
        addCloudMineHeader(get);
        return get;
    }

    private HttpGet createGetFile(String key) {
        HttpGet get = new HttpGet(baseUrl.binary(key).urlString());
        addCloudMineHeader(get);
        return get;
    }

    private HttpGet createGetObjects(Collection<String> keys) {
        HttpGet get = new HttpGet(baseUrl.text().keys(keys).urlString());
        addCloudMineHeader(get);
        return get;
    }

    private HttpPost createResetPasswordConfirmation(String token, String newPassword) {
        HttpPost post = new HttpPost(baseUrl.account().password().reset().addAction(token).urlString());
        addCloudMineHeader(post);
        addJson(post, JsonUtilities.jsonCollection(
                JsonUtilities.createJsonProperty(PASSWORD_KEY, newPassword)
        ));
        return post;
    }

    private HttpPost createResetPassword(String email) {
        HttpPost post = new HttpPost(baseUrl.account().password().reset().urlString());
        addCloudMineHeader(post);
        addJson(post, JsonUtilities.jsonCollection(
                JsonUtilities.createJsonProperty(EMAIL_KEY, email)
        ));
        return post;
    }

    private HttpPost createChangePassword(CMUser user, String newPassword) {
        HttpPost post = new HttpPost(baseUrl.account().password().change().urlString());
        addCloudMineHeader(post);
        addAuthorizationHeader(user, post);
        addJson(post, JsonUtilities.jsonCollection(
                JsonUtilities.createJsonProperty(PASSWORD_KEY, newPassword)));
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

    private void addJson(HttpEntityEnclosingRequestBase message, Json json) {
        addJson(message, json.asJson());
    }

    private void addAuthorizationHeader(CMUser user, HttpEntityEnclosingRequestBase post) {
        post.addHeader(AUTHORIZATION_KEY, "Basic " + user.encode());
    }

    protected void addCloudMineHeader(AbstractHttpMessage message) {
        message.addHeader(CMApiCredentials.cloudMineHeader());
        message.addHeader(new BasicHeader(AGENT_HEADER_KEY, cloudMineAgent()));
    }

    protected String cloudMineAgent() {
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

    protected ResponseConstructor<LogInResponse> logInResponseConstructor() {
        return LogInResponse.CONSTRUCTOR;
    }

    private ResponseConstructor<SimpleCMObjectResponse> simpleCMObjectResponseConstructor() {
        return SimpleCMObjectResponse.CONSTRUCTOR;
    }
}

package com.cloudmine.api.rest;

import android.os.Parcel;
import android.os.Parcelable;
import com.cloudmine.api.*;
import com.cloudmine.api.rest.callbacks.WebServiceCallback;
import com.cloudmine.api.rest.response.*;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.ResponseConstructor;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicHeaderElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/16/12, 2:34 PM
 */
public class CloudMineWebService implements Parcelable{
    private static CloudMineWebService lastInstantiatedInstance;

    /**
     * Returns the last instantiated instance of CloudMineWebService.
     * @return
     */
    public static CloudMineWebService defaultService() {
        return lastInstantiatedInstance;
    }

    public static final Header JSON_HEADER = new Header() {
        public String getName() {
            return "Content-Type";
        }

        public String getValue() {
            return "application/json";
        }

        public HeaderElement[] getElements() {
            return new HeaderElement[] { new BasicHeaderElement(getName(), getValue()) };
        }

    };
    public static final Creator<CloudMineWebService> CREATOR =
            new Creator<CloudMineWebService>() {

                @Override
                public CloudMineWebService createFromParcel(Parcel parcel) {
                    return new CloudMineWebService(parcel);
                }

                @Override
                public CloudMineWebService[] newArray(int i) {
                    return new CloudMineWebService[i];
                }
            };
    private static final Logger LOG = LoggerFactory.getLogger(CloudMineWebService.class);

    private final CloudMineURLBuilder baseUrl;
    private final HttpClient httpClient = new DefaultHttpClient();
    private final AsynchronousHttpClient asyncHttpClient; //TODO split this into an asynch and synch impl instead of both in one

    public CloudMineWebService(String appId) {
        this(new CloudMineURLBuilder(appId), new AndroidAsynchronousHttpClient());
    }

    public CloudMineWebService(CloudMineURLBuilder baseUrl, AsynchronousHttpClient asyncClient) {
        this.baseUrl = baseUrl;
        asyncHttpClient = asyncClient;
        lastInstantiatedInstance = this;
    }

    public CloudMineWebService(Parcel in) {
        this(in.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(baseUrl.appPath());
    }

    public UserCloudMineWebService userWebService(UserToken token) {
        return new UserCloudMineWebService(baseUrl.user(), token, asyncHttpClient);
    }

    public UserCloudMineWebService userWebService(CloudMineResponse response) {
        return userWebService(new UserToken(response.asJson()));
    }

    public ObjectModificationResponse deleteAll() {
        return executeCommand(createDeleteAll(), ObjectModificationResponse.CONSTRUCTOR);
    }

    public ObjectModificationResponse delete(Collection<String> keys) {
        return executeCommand(createDelete(keys), ObjectModificationResponse.CONSTRUCTOR);
    }

    public ObjectModificationResponse delete(String key) {
        return executeCommand(createDelete(key), ObjectModificationResponse.CONSTRUCTOR);
    }

    public Future<SimpleObjectResponse> allObjectsOfClass(String klass) {
        return allObjectsOfClass(klass, WebServiceCallback.DO_NOTHING);
    }

    public Future<SimpleObjectResponse> allObjectsOfClass(String klass, WebServiceCallback callback) {
        HttpGet search = createSearch("[" + JsonUtilities.CLASS_KEY + "=" + JsonUtilities.addQuotes(klass) + "]");
        return executeAsyncCommand(search, callback, SimpleObjectResponse.CONSTRUCTOR);
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

    public Future<ObjectModificationResponse> asyncDelete(Collection<String> keys) {
        return asyncDelete(keys, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncDelete(Collection<String> keys, WebServiceCallback callback) {
        return executeAsyncCommand(createDelete(keys), callback, ObjectModificationResponse.CONSTRUCTOR);
    }

    public Future<FileCreationResponse> asyncUpload(CloudMineFile file) {
        return asyncUpload(file, WebServiceCallback.DO_NOTHING);
    }

    public Future<FileCreationResponse> asyncUpload(CloudMineFile file, WebServiceCallback callback) {
        return executeAsyncCommand(createPut(file), callback, FileCreationResponse.CONSTRUCTOR);
    }

    public Future<CloudMineFile> asyncLoadFile(String key) {
        return asyncLoadFile(key, WebServiceCallback.DO_NOTHING);
    }

    public Future<CloudMineFile> asyncLoadFile(String key, WebServiceCallback callback) {
        return executeAsyncCommand(createGetFile(key), callback, CloudMineFile.constructor(key));
    }

    public Future<SimpleObjectResponse> asyncLoadObjects() {
        return asyncLoadObjects(WebServiceCallback.DO_NOTHING);
    }

    public Future<SimpleObjectResponse> asyncLoadObjects(WebServiceCallback callback) {
        return asyncLoadObjects(Collections.<String>emptyList(), callback);
    }

    public Future<SimpleObjectResponse> asyncLoadObject(String key) {
        return asyncLoadObject(key, WebServiceCallback.DO_NOTHING);
    }

    public Future<SimpleObjectResponse> asyncLoadObject(String key, WebServiceCallback callback) {
        return asyncLoadObjects(Collections.<String>singleton(key), callback);
    }

    public Future<SimpleObjectResponse> asyncLoadObjects(Collection<String> keys) {
        return asyncLoadObjects(keys, WebServiceCallback.DO_NOTHING);
    }

    public Future<SimpleObjectResponse> asyncLoadObjects(Collection<String> keys, WebServiceCallback callback) {
        return executeAsyncCommand(createGetObjects(keys), callback, SimpleObjectResponse.CONSTRUCTOR);
    }

    public Future<ObjectModificationResponse> asyncInsert(SimpleCMObject toCreate) {
        return asyncInsert(toCreate, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncInsert(SimpleCMObject toCreate, WebServiceCallback callback) {
        return executeAsyncCommand(createPut(toCreate.asJson()), callback, ObjectModificationResponse.CONSTRUCTOR);
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
        );
        return executeAsyncCommand(createPut(jsonStringsCollection), callback, ObjectModificationResponse.CONSTRUCTOR);
    }

    public Future<ObjectModificationResponse> asyncUpdate(SimpleCMObject toUpdate) {
        return asyncUpdate(toUpdate, WebServiceCallback.DO_NOTHING);
    }

    public Future<ObjectModificationResponse> asyncUpdate(SimpleCMObject toUpdate, WebServiceCallback callback) {
        return executeAsyncCommand(createJsonPost(toUpdate.asJson()), callback, ObjectModificationResponse.CONSTRUCTOR);
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
        String json = JsonUtilities.jsonCollection(jsonStrings);
        return executeAsyncCommand(createJsonPost(json), callback, ObjectModificationResponse.CONSTRUCTOR);
    }

    public SimpleObjectResponse get() {
        return executeCommand(createGet(), SimpleObjectResponse.CONSTRUCTOR);
    }

    public CloudMineFile getFile(String key) {
        try {
            HttpResponse response = httpClient.execute(createGetFile(key));
            return new CloudMineFile(response, key);
        } catch (IOException e) {
            //TODO handle this
        }
        return null;
    }

    public CloudMineResponse search(String searchString) {
        HttpGet get = createSearch(searchString);
        return executeCommand(get);
    }

    public ObjectModificationResponse set(String json) {
        HttpPut put = createPut(json);
        return executeCommand(put, ObjectModificationResponse.CONSTRUCTOR);
    }

    public ObjectModificationResponse update(String json) {
        HttpPost post = createJsonPost(json);
        return executeCommand(post, ObjectModificationResponse.CONSTRUCTOR);
    }

    public FileCreationResponse set(CloudMineFile file) {
        return executeCommand(createPut(file), FileCreationResponse.CONSTRUCTOR);
    }

    public Future<CloudMineResponse> asyncCreateUser(User user) {
        return executeAsyncCommand(createPut(user));
    }

    public Future<CloudMineResponse> asyncCreateUser(User user, WebServiceCallback callback) {
        return executeAsyncCommand(createPut(user), callback, CloudMineResponse.CONSTRUCTOR);
    }

    public Future<LogInResponse> asyncLogin(User user) {
        return asyncLogin(user, WebServiceCallback.DO_NOTHING);
    }

    public Future<LogInResponse> asyncLogin(User user, WebServiceCallback callback) {
        return executeAsyncCommand(createLoginPost(user), callback, LogInResponse.CONSTRUCTOR);
    }

    public Future<CloudMineResponse> asyncLogout(UserToken token) {
        return asyncLogout(token, WebServiceCallback.DO_NOTHING);
    }

    public Future<CloudMineResponse> asyncLogout(UserToken token, WebServiceCallback callback) {
        return executeAsyncCommand(createLogoutPost(token), callback, CloudMineResponse.CONSTRUCTOR);
    }


    public CloudMineResponse set(User user) {
        return executeCommand(createPut(user));
    }

    public LogInResponse login(User user) {
        return executeCommand(createLoginPost(user), LogInResponse.CONSTRUCTOR);
    }

    public CloudMineResponse logout(UserToken sessionToken) {
        return executeCommand(createLogoutPost(sessionToken));
    }

    private CloudMineResponse executeCommand(HttpUriRequest message) {
        return executeCommand(message, CloudMineResponse.CONSTRUCTOR);
    }

    private Future<CloudMineResponse> executeAsyncCommand(HttpUriRequest message) {
        return executeAsyncCommand(message, WebServiceCallback.DO_NOTHING, CloudMineResponse.CONSTRUCTOR);
    }

    private Future<CloudMineResponse> executeAsyncCommand(HttpUriRequest message, WebServiceCallback callback) {
        return executeAsyncCommand(message, callback, CloudMineResponse.CONSTRUCTOR);
    }

    private <T> Future<T> executeAsyncCommand(HttpUriRequest message, WebServiceCallback callback, ResponseConstructor<T> constructor) {
        return constructor.constructFuture(asyncHttpClient.executeCommand(message, callback, constructor));
    }

    private <T extends CloudMineResponse> T executeCommand(HttpUriRequest message, ResponseConstructor<T> constructor) {
        HttpResponse response = null;
        try {
            response = httpClient.execute(message);
            return constructor.construct(response);
        }
        catch (IOException e) {
            LOG.error("Error executing command: " + message.getURI(), e);
        }
        finally {
            AsyncHttpClient.consumeEntityResponse(response);
        }
        return constructor.construct(null);
    }

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

    private HttpPut createPut(User user) {
        HttpPut put = new HttpPut(baseUrl.account().create().urlString());
        addCloudMineHeader(put);
        addJson(put, user.asJson());
        return put;
    }

    private HttpPut createPut(CloudMineFile file) {
        HttpPut put = new HttpPut(baseUrl.binary(file.getKey()).urlString());
        addCloudMineHeader(put);
        put.setEntity(new ByteArrayEntity(file.getFileContents()));
        put.addHeader("Content-Type", file.getContentType());
        return put;
    }

    private HttpPost createJsonPost(String json) {
        HttpPost post = createPost(baseUrl.text().urlString());
        addJson(post, json);
        return post;
    }

    private HttpPost createLoginPost(User user) {
        HttpPost post = createPost(baseUrl.account().login().urlString());
        post.addHeader("Authorization", "Basic " + user.encode());
        return post;
    }

    private HttpPost createLogoutPost(UserToken sessionToken) {
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

    private void addJson(HttpEntityEnclosingRequestBase message, String json) {
        if(json == null)
            json = "";
        if(!message.containsHeader(JSON_HEADER.getName())) {
            message.addHeader(JSON_HEADER);
        }
        try {
            message.setEntity(new StringEntity(json, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOG.error("Error encoding json", e);
        }
    }

    protected void addCloudMineHeader(AbstractHttpMessage message) {
        message.addHeader(ApiCredentials.cloudMineHeader());
    }
}

package com.cloudmine.api.rest;

import android.os.Parcel;
import android.os.Parcelable;
import com.cloudmine.api.*;
import com.cloudmine.api.rest.callbacks.WebServiceCallback;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/16/12, 2:34 PM
 */
public class CloudMineWebService implements Parcelable{
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

    public CloudMineResponse deleteAll() {
        return executeCommand(createDeleteAll());
    }

    public CloudMineResponse delete(String... keys) {
        return executeCommand(createDelete(keys));
    }

    public Future<SimpleObjectResponse> allObjectsOfClass(String klass) {
        return allObjectsOfClass(klass, WebServiceCallback.DO_NOTHING);
    }

    public Future<SimpleObjectResponse> allObjectsOfClass(String klass, WebServiceCallback callback) {
        HttpGet search = createSearch("[" + JsonUtilities.CLASS_KEY + "=" + JsonUtilities.addQuotes(klass) + "]");
        return executeAsyncCommand(search, callback, SimpleObjectResponse.CONSTRUCTOR);
    }

    public Future<CloudMineResponse> create(SimpleCMObject object) {
        return create(object, WebServiceCallback.DO_NOTHING);
    }

    public Future<CloudMineResponse> create(SimpleCMObject object, WebServiceCallback callback) {
        return executeAsyncCommand(createPut(object.asJson()), callback);
    }

    public Future<CloudMineResponse> createAll(WebServiceCallback callback, SimpleCMObject... toCreate) {
        List<Json> jsonStrings = new ArrayList<Json>(toCreate.length);
        for(SimpleCMObject object : toCreate) {
            jsonStrings.add(new JsonString(object.asKeyedObject()));
        }
        return executeAsyncCommand(createPut(JsonUtilities.jsonCollection(toCreate)));
    }

    public CloudMineResponse get() {
        return executeCommand(createGet());
    }

    public CloudMineFile getObject(String key) {
        try {
            HttpResponse response = httpClient.execute(createGetObject(key));
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

    public CloudMineResponse set(String json) {
        HttpPut put = createPut(json);
        return executeCommand(put);
    }

    public CloudMineResponse update(String json) {
        HttpPost post = createJsonPost(json);
        return executeCommand(post);
    }

    public CloudMineResponse set(CloudMineFile file) {
        return executeCommand(createPut(file));
    }

    public Future<CloudMineResponse> asyncCreateUser(User user) {
        return executeAsyncCommand(createPut(user));
    }

    public Future<CloudMineResponse> asyncCreateUser(User user, WebServiceCallback callback) {
        return executeAsyncCommand(createPut(user), callback, CloudMineResponse.CONSTRUCTOR);
    }

    public Future<LoginResponse> asyncLogin(User user) {
        return asyncLogin(user, WebServiceCallback.DO_NOTHING);
    }

    public Future<LoginResponse> asyncLogin(User user, WebServiceCallback callback) {
        return executeAsyncCommand(createLoginPost(user), callback, LoginResponse.CONSTRUCTOR);
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

    public LoginResponse login(User user) {
        return executeCommand(createLoginPost(user), LoginResponse.CONSTRUCTOR);
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

    private <T extends CloudMineResponse> Future<T> executeAsyncCommand(HttpUriRequest message, WebServiceCallback callback, CloudMineResponse.ResponseConstructor<T> constructor) {
        return constructor.constructFuture(asyncHttpClient.executeCommand(message, callback));
    }

    private <T extends CloudMineResponse> T executeCommand(HttpUriRequest message, CloudMineResponse.ResponseConstructor<T> constructor) {
        HttpResponse response = null;
        try {
            response = httpClient.execute(message);
            return constructor.construct(response);
        }
        catch (IOException e) {
            LOG.error("Error executing command: " + message.getURI(), e);
        }
        finally {
            consumeEntityResponse(response);
        }
        return constructor.construct(null);
    }

    /**
     * If the entity response is not fully consumed, the connection will not be released
     * @param response
     */
    private void consumeEntityResponse(HttpResponse response) {
        if(response != null && response.getEntity() != null) {
            HttpEntity body = response.getEntity();
            if(body.isStreaming()) {
                try {
                    InputStream instream = body.getContent();
                    if(instream != null) {
                            instream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
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

    private HttpDelete createDelete(String... keys) {
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

    private HttpGet createGetObject(String key) {
        HttpGet get = new HttpGet(baseUrl.binary(key).urlString());
        addCloudMineHeader(get);
        return get;
    }

    private void addJson(HttpEntityEnclosingRequestBase message, String json) {
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

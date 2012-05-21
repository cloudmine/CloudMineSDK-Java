package com.cloudmine.api.rest;

import com.cloudmine.api.ApiCredentials;
import com.cloudmine.api.CloudMineFile;
import com.cloudmine.api.User;
import com.cloudmine.api.UserToken;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/16/12, 2:34 PM
 */
public class CloudMineStore {
    public static final BasicHeader JSON_HEADER = new BasicHeader("Content-Type", "application/json");
    private static final Logger LOG = LoggerFactory.getLogger(CloudMineStore.class);

    private final CloudMineURLBuilder baseUrl;
    private final HttpClient httpClient = new DefaultHttpClient();
//    private final HttpAsyncClient asyncHttpClient = new DefaultHttpAsyncClient();

    public CloudMineStore(CloudMineURLBuilder baseUrl) {
        this.baseUrl = baseUrl;
    }

    public CloudMineResponse deleteAll() {
        return executeCommand(createDeleteAll());
    }

    public CloudMineResponse delete(String... keys) {
        return executeCommand(createDelete(keys));
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
            EntityUtils.consumeQuietly(response.getEntity());
        }
    }

//
//    public void get(ResponseCallback callback) {
//        asyncHttpClient.start();
//        try {
//        asyncHttpClient.execute(createGet(), callback);
//        } finally {
//            try {
//                asyncHttpClient.shutdown();
//            } catch (InterruptedException e) {
//                //NO GIVES NO FUCKS
//            }
//        }
//    }
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
        message.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
    }

    private void addCloudMineHeader(AbstractHttpMessage message) {
        message.addHeader(ApiCredentials.cloudMineHeader());
    }
}

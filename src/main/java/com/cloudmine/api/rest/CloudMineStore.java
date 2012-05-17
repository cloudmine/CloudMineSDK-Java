package com.cloudmine.api.rest;

import com.cloudmine.api.ApiCredentials;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
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

    public CloudMineResponse get() {
        return executeCommand(createGet());
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
        HttpPost post = createPost(json);
        return executeCommand(post);
    }

    private CloudMineResponse executeCommand(HttpUriRequest message) {
        HttpResponse response = null;
        try {
            response = httpClient.execute(message);
            return new CloudMineResponse(response);
        }
        catch (IOException e) {
            LOG.error("Error executing command: " + message.getURI(), e);
        }
        finally {
            consumeEntityResponse(response);
        }
        return new CloudMineResponse(null);
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
        java.net.URL uri = baseUrl.deleteAll().url();
        HttpDelete delete = new HttpDelete(uri.toString());
        addCloudMineHeader(delete);
        return delete;
    }

    private HttpPut createPut(String json) {
        HttpPut put = new HttpPut(baseUrl.text().url().toString());
        addCloudMineHeader(put);
        addJson(put, json);
        return put;
    }

    private HttpPost createPost(String json) {
        HttpPost post = new HttpPost(baseUrl.text().url().toString());
        addCloudMineHeader(post);
        addJson(post, json);
        return post;
    }

    private HttpGet createGet() {
        HttpGet get = new HttpGet(baseUrl.text().url().toString());
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

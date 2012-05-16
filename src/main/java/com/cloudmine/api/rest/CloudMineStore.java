package com.cloudmine.api.rest;

import com.cloudmine.api.ApiCredentials;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;

import java.io.IOException;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/16/12, 2:34 PM
 */
public class CloudMineStore {
    public static final BasicHeader JSON_HEADER = new BasicHeader("Content-Type", "application/json");

    private final CloudMineURLBuilder baseUrl;
    private final HttpClient httpClient = new DefaultHttpClient();
//    private final HttpAsyncClient asyncHttpClient = new DefaultHttpAsyncClient();

    public CloudMineStore(CloudMineURLBuilder baseUrl) {
        this.baseUrl = baseUrl;
    }

    public CloudMineResponse get() {
        try {
            HttpResponse response = httpClient.execute(createGet());
            return new CloudMineResponse(response);
        } catch (IOException e) {
            //TODO return this somehow? thrown exceptions are dum
        }
        return null; //TODO never return null
    }

    public CloudMineResponse put(String json) {
        HttpPut put = createPut(json);
        try {
            HttpResponse response = httpClient.execute(put);
            return new CloudMineResponse(response);
        } catch (IOException e) {
            e.printStackTrace(); //TODO same as above
        }
        return null;
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

    private HttpPut createPut(String json) {
        HttpPut put = new HttpPut(baseUrl.text());
        put.addHeader(ApiCredentials.cloudMineHeader());
        put.addHeader(JSON_HEADER);
        put.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
        return put;
    }

    private HttpGet createGet() {
        HttpGet get = new HttpGet(baseUrl.text());
        get.setHeader(ApiCredentials.cloudMineHeader());
        return get;
    }
}

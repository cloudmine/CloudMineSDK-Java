package com.cloudmine.api.rest;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/16/12, 10:54 AM
 */
public class URLBuilder extends BaseURLBuilder<URLBuilder> {

    public URLBuilder(String baseUrl) {
        super(baseUrl);
    }

    protected URLBuilder(String baseUrl, String actions, String queryParams) {
        super(baseUrl, actions, queryParams);
    }

    @Override
    protected URLBuilder newBuilder(String baseUrl, String actions, String queryParams) {
        return new URLBuilder(baseUrl, actions, queryParams);
    }

}

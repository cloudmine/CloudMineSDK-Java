package com.cloudmine.api.rest;

/**
 * A generic Builder for URL strings
 * Copyright CloudMine LLC
 */
public class URLBuilder extends BaseURLBuilder<URLBuilder> {

    /**
     * Instantiate a new URLBuilder with the specified base url
     * @param baseUrl the beginning of the URL
     */
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

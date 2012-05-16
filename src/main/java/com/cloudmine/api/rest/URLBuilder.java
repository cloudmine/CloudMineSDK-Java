package com.cloudmine.api.rest;

import org.apache.http.NameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/16/12, 10:54 AM
 */
public class URLBuilder implements URL {
    protected final String baseUrl;
    protected final List<String> actions = new ArrayList<String>();
    protected final List<String> queryParams = new ArrayList<String>();

    public URLBuilder(String baseUrl) {
        if(baseUrl == null)
            throw new NullPointerException("URLBuilder cannot build on null baseUrl");
        baseUrl = removeEndSeperator(baseUrl);
        this.baseUrl = baseUrl;
    }

    public URLBuilder addAction(String action) {
        actions.add(formatUrlPart(action));
        return this;
    }

    public URLBuilder addQuery(String key, String value) {
        queryParams.add(toQueryParam(key, value));
        return this;
    }

    public URLBuilder addQuery(NameValuePair param) {
        return addQuery(param.getName(), param.getValue());
    }

    public String baseUrl() {
        return baseUrl;
    }

    protected static String toQueryParam(String key, String value) {
        return "?" + key + "=" + value;
    }

    protected static String formatUrlPart(String url) {
        return removeEndSeperator(
                startWithSeperator(url));
    }

    protected static String removeEndSeperator(String url) {
        if(url.endsWith(SEPARATOR)) {
            int endOfString = url.length() - SEPARATOR.length();
            url = url.substring(0, endOfString);
        }
        return url;
    }

    protected static String startWithSeperator(String url) {
        if(url.startsWith(SEPARATOR)) {
            return url;
        }
        return SEPARATOR + url;
    }

    @Override
    public String url() {
        StringBuilder strBuilder = new StringBuilder(baseUrl());
        for(String action : actions) {
            strBuilder.append(action);
        }
        for(String query : queryParams) {
            strBuilder.append(query);
        }
        return strBuilder.toString();
    }

    @Override
    public String toString() {
        return url();
    }

    @Override
    public boolean equals(Object other) {
        if(other != null && other instanceof URLBuilder) { //This will allow this to equal subclasses of the URL builder
            return ((URLBuilder)other).url().equals(this.url());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return url().hashCode();
    }
}

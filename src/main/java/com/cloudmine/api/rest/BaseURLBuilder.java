package com.cloudmine.api.rest;

import org.apache.http.NameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class only exists to make Java be cool about types. Should never be directly instantiated
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/17/12, 10:48 AM
 */
public abstract class BaseURLBuilder<T> implements BaseURL {
    private static final Logger LOG = LoggerFactory.getLogger(BaseURLBuilder.class);
    protected final String baseUrl;
    protected final String actions;
    protected final String queryParams;



    public BaseURLBuilder(String baseUrl) {
        this(baseUrl, "", "");
    }

    protected BaseURLBuilder(String baseUrl, String actions, String queryParams) {
        if(baseUrl == null)
            throw new NullPointerException("URLBuilder cannot build on null baseUrl");
        baseUrl = removeEndSeperator(baseUrl);
        this.baseUrl = baseUrl;
        this.actions = actions;
        this.queryParams = queryParams;
    }

    protected abstract T newBuilder(String baseUrl, String actions, String queryParams);

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

    public T addAction(String action) {
        return newBuilder(baseUrl, actions + formatUrlPart(action), queryParams);
    }

    public T addQuery(String key, String value) {
        return newBuilder(baseUrl, actions, queryParams + toQueryParam(key, value));
    }

    public T addQuery(NameValuePair param) {
        return addQuery(param.getName(), param.getValue());
    }

    public String baseUrl() {
        return baseUrl;
    }

    public URL url() {
        try {
            URL url = new URL(urlString());
            return url;
        } catch (MalformedURLException e) {
            LOG.error("URL was malformed", e);
        }
        return null;
    }

    @Override
    public String urlString() {
        String url = baseUrl() + actions + queryParams;
        return url;
    }

    @Override
    public String toString() {
        return urlString();
    }

    @Override
    public boolean equals(Object other) {
        if(other != null && other instanceof BaseURLBuilder) { //This will allow this to equal subclasses of the URL builder
            return ((BaseURLBuilder)other).urlString().equals(this.urlString());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return urlString().hashCode();
    }
}

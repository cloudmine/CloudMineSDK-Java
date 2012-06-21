package com.cloudmine.api.rest;

import com.cloudmine.api.exceptions.CreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class only exists to make Java be cool about types. Should never be directly instantiated
 * Helps build URLs. Immutable, so any method that appears to mutate returns the new copy.
 * Copyright CloudMine LLC
 */
public abstract class BaseURLBuilder<T> implements BaseURL {
    private static final Logger LOG = LoggerFactory.getLogger(BaseURLBuilder.class);
    protected final String baseUrl;
    protected final String actions;
    protected final String queryParams;

    /**
     * Instantiate a new BaseURLBuilder
     * @param baseUrl the beginning part of the URL. Actions and query params will be appended to this
     */
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

    protected String toQueryParam(String key, String value) {
        return key + "=" + value;
    }

    private String querySeparator() {
        return queryParams == null || queryParams.isEmpty() ?
                "?" :
                "&";
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

    /**
     * Add the given action to the URL, and return the new builder
     * @param action the action to add. any trailing /'s are removed, and if the action does not begin with /, one is added.
     *               So addAction("/foo/") and addAction("foo") are equivalent
     * @return a new builder with the added action
     */
    public T addAction(String action) {
        return newBuilder(baseUrl, actions + formatUrlPart(action), queryParams);
    }

    /**
     * Remove the given action from the url, if it exists. Then returns the new builder
     * @param action the action to remove, trailing /'s will be removed and leading / will be added if needed
     * @return a new builder without the added action
     */
    public T removeAction(String action) {
        String formattedAction = formatUrlPart(action);
        String newActions = actions.replace(formattedAction, "");
        return newBuilder(baseUrl, newActions, queryParams);
    }

    /**
     * Add the given query parameter to the URL, and returns the new builder. Query params are formated to
     * look like "?key=value"
     * @param key the query param key
     * @param value the query param value
     * @return the new builder with the added query param
     */
    public T addQuery(String key, String value) {
        return addQuery(toQueryParam(key, value));
    }

    public T addQuery(String query) {
        return newBuilder(baseUrl, actions, queryParams + querySeparator() +  query);
    }

    /**
     * Return just the base part of this URL.
     * @return the base part of the url, which was passed to the constructor when this URLBuilder was instantiated
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * This URLBuilder as a URL
     * @return URL representation of this URLBuilder
     * @throws CreationException if the URL is malformed
     */
    public URL getUrl() throws CreationException {
        try {
            URL url = new URL(asUrlString());
            return url;
        } catch (MalformedURLException e) {
            LOG.error("URL was malformed", e);
            throw new CreationException("Malformed URL: " + asUrlString(), e);
        }
    }

    @Override
    public String asUrlString() {
        String url = getBaseUrl() + actions + queryParams;
        return url;
    }

    @Override
    public String toString() {
        return asUrlString();
    }

    @Override
    public boolean equals(Object other) {
        if(other != null && other instanceof BaseURLBuilder) { //This will allow this to equal subclasses of the URL builder
            return ((BaseURLBuilder)other).asUrlString().equals(this.asUrlString());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return asUrlString().hashCode();
    }
}

package com.cloudmine.api.rest;

import com.cloudmine.api.CMApiCredentials;
import com.cloudmine.api.CMRequestOptions;
import com.cloudmine.api.exceptions.CreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;

/**
 * Helps with creating CloudMine service URLs. You probably have no reason to instantiate this class directly
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class CMURLBuilder extends BaseURLBuilder<CMURLBuilder> {

    public static final String USER = "user";

    enum VERSION implements BaseURL {
        V1("/v1");
        private final String urlRepresentation;
        private VERSION(String urlRepresentation) {
            this.urlRepresentation = urlRepresentation;
        }

        @Override
        public String asUrlString() {
            return urlRepresentation;
        }

        @Override
        public String toString() {
            return asUrlString();
        }
    }
    private static final Logger LOG = LoggerFactory.getLogger(CMURLBuilder.class);
    public static final VERSION DEFAULT_VERSION = VERSION.V1;
    public static final String CLOUD_MINE_URL = "https://api.cloudmine.me";
    public static final String APP = "/app";


    /**
     * Creates a default base url for the application id defined by calling {@link CMApiCredentials#initialize(String, String)}
     * @throws CreationException if {@link CMApiCredentials#initialize(String, String)} has not been called yet
     */
    public CMURLBuilder() throws CreationException {
        this(CMApiCredentials.getApplicationIdentifier());
    }

    /**
     * Creates a base url builder for the specified application identifier
     * @param appId the application id, found in the CloudMine developer dashboard
     */
    public CMURLBuilder(String appId) {
        this(CLOUD_MINE_URL, appId);
    }

    /**
     * Creates a base url builder for a non standard base CloudMine url, eg https://api.beta.cloudmine.me
     * @param cloudMineUrl the base part of the url
     * @param appId the application identifier, found in the CloudMine developer dashboard
     */
    protected CMURLBuilder(String cloudMineUrl, String appId) {
        this(cloudMineUrl + DEFAULT_VERSION + APP + formatUrlPart(appId), "", "");
    }

    protected CMURLBuilder(String baseUrl, String actions, String queryParams) {
        super(baseUrl, actions, queryParams);

    }

    protected CMURLBuilder newBuilder(String baseUrl, String actions, String queryParams) {
        return new CMURLBuilder(baseUrl, actions, queryParams);
    }

    protected static String extractAppId(String url) {
        if(url == null)
            return url;
        String[] urlParts = url.split(APP, 2);
        if(urlParts.length != 2) {
            return null;
        }
        return urlParts[1];
    }

    /**
     * Return all of this url after the /app/ portion
     * @return all of this url after the /app/ portion
     */
    public String getApplicationPath() {
        return extractAppId(asUrlString());
    }

    /**
     * Return the query part of this url
     * @return the query part of this url
     */
    public String getQueries() {
        return queryParams;
    }

    /**
     * Add a search query to this URL. The search portion of the query will be encoded
     * @param search a search query
     * @return a new CMURLBuilder with the given search query
     */
    public CMURLBuilder search(String search) {
        String encodedSearch;
        encodedSearch = encode(search);
        return addAction("search").addQuery("q", encodedSearch);
    }

    public static String encode(String url) {
        String encodedSearch;
        try {
            encodedSearch = URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            encodedSearch = url;
            LOG.error("Error encoding search string: " + url, e);
        }
        return encodedSearch;
    }

    public CMURLBuilder options(CMRequestOptions options) {
        return addQuery(options.asUrlString());
    }

    /**
     * Add the data action
     * @return a new CMURLBuilder with a data action
     */
    public CMURLBuilder data() {
        return addAction("data");
    }

    /**
     * Add the delete all action
     * @return a new CMURLBuilder with delete all equal to true
     */
    public CMURLBuilder deleteAll() {
        return data().addQuery("all", "true");
    }

    /**
     * Add the specified objectIds to this url, to be deleted
     * @param objectIds the top level objectIds to delete
     * @return a new CMURLBuilder with all the specified objectIds to be deleted
     */
    public CMURLBuilder delete(Collection<String> objectIds) {
        return data().objectIds(objectIds);
    }

    /**
     * Equivalent to passing Collections.singleton(objectId) to {@link #delete(java.util.Collection)}
     * @param objectId the object ids to delete
     * @return a new CMURLBuilder with the specified objectId to delete
     */
    public CMURLBuilder delete(String objectId) {
        return data().objectId(objectId);
    }

    /**
     * Add the specified objectId as a query. Equivalent to passing Collections.singleton(objectId) to {@link #objectIds(java.util.Collection)}
     * @param objectId
     * @return a new CMURLBuilder with the specified object id as a query
     */
    public CMURLBuilder objectId(String objectId) {
        if(objectId == null)
            return this;
        return addQuery("keys", objectId);
    }

    /**
     * Add the specified objectIds as a query.
     * @param objectIds
     * @return
     */
    public CMURLBuilder objectIds(Collection<String> objectIds) {
        if(objectIds == null || objectIds.size() == 0)
            return this;
        return addQuery("keys", keysToString(objectIds));
    }

    private String keysToString(Collection<String> keys) {
        String keyString = "";
        String comma = "";
        for(String key : keys) {
            keyString += comma + encode(key);
            comma = ",";
        }
        return keyString;
    }

    public CMURLBuilder notUser() {
        return this.removeAction(USER);
    }

    public CMURLBuilder account() {
        return this.notUser().addAction("account");
    }

    public CMURLBuilder reset() {
        return this.addAction("reset");
    }

    public CMURLBuilder password() {
        return this.addAction("password");
    }

    public CMURLBuilder change() {
        return this.addAction("change");
    }

    public CMURLBuilder login() {
        return this.addAction("login");
    }

    public CMURLBuilder logout() {
        return this.addAction("logout");
    }

    public CMURLBuilder user() {
        return this.addAction(USER);
    }

    public CMURLBuilder create() {
        return this.addAction("create");
    }

    public CMURLBuilder text() {
        return this.addAction("text");
    }

    public CMURLBuilder binary() {
        return this.addAction("binary");
    }

    public CMURLBuilder binary(String key) {
        return this.binary().addKey(key);
    }

    /**
     * Adds the key as an action if it exists
     * @param key
     * @return
     */
    public CMURLBuilder addKey(String key) {
        if(key != null) {
            return this.addAction(key);
        }
        return this;
    }
}

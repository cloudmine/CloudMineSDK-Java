package com.cloudmine.api.rest;

import com.cloudmine.api.CMApiCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/16/12, 11:21 AM
 */
public class CMURLBuilder extends BaseURLBuilder<CMURLBuilder> {
    enum VERSION implements BaseURL {
        V1("/v1");
        private final String urlRepresentation;
        private VERSION(String urlRepresentation) {
            this.urlRepresentation = urlRepresentation;
        }

        @Override
        public String urlString() {
            return urlRepresentation;
        }

        @Override
        public String toString() {
            return urlString();
        }
    }
    private static final Logger LOG = LoggerFactory.getLogger(CMURLBuilder.class);
    public static final VERSION DEFAULT_VERSION = VERSION.V1;
    public static final String CLOUD_MINE_URL = "https://api.cloudmine.me";
    public static final String APP = "/app";


    public CMURLBuilder() {
        this(CMApiCredentials.applicationIdentifier());
    }

    public CMURLBuilder(String appId) {
        this(CLOUD_MINE_URL, appId);
    }

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

    public String appPath() {
        return extractAppId(urlString());
    }

    public String queries() {
        return queryParams;
    }

    public CMURLBuilder search(String search) {
        String encodedSearch;
        try {
            encodedSearch = URLEncoder.encode(search, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            encodedSearch = search;
            LOG.error("Error encoding search string: " + search, e);
        }
        return addAction("search").addQuery("q", encodedSearch);
    }

    public CMURLBuilder data() {
        return addAction("data");
    }

    public CMURLBuilder deleteAll() {
        return data().addQuery("all", "true");
    }

    public CMURLBuilder delete(Collection<String> keys) {
        return data().keys(keys);
    }

    public CMURLBuilder delete(String key) {
        return data().key(key);
    }

    public CMURLBuilder key(String key) {
        if(key == null)
            return this;
        return addQuery("keys", key);
    }

    public CMURLBuilder keys(Collection<String> keys) {
        if(keys == null || keys.size() == 0)
            return this;
        return addQuery("keys", keysToString(keys));
    }

    private String keysToString(Collection<String> keys) {
        String keyString = "";
        String comma = "";
        for(String key : keys) {
            keyString += comma + key;
            comma = ",";
        }
        return keyString;
    }

    public CMURLBuilder account() {
        return this.addAction("account");
    }

    public CMURLBuilder login() {
        return this.addAction("login");
    }

    public CMURLBuilder logout() {
        return this.addAction("logout");
    }

    public CMURLBuilder user() {
        return this.addAction("user");
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

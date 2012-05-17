package com.cloudmine.api.rest;

import com.cloudmine.api.ApiCredentials;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/16/12, 11:21 AM
 */
public class CloudMineURLBuilder extends BaseURLBuilder<CloudMineURLBuilder> {
    enum VERSION implements URL {
        V1("/v1");
        private final String urlRepresentation;
        private VERSION(String urlRepresentation) {
            this.urlRepresentation = urlRepresentation;
        }

        @Override
        public String url() {
            return urlRepresentation;
        }

        @Override
        public String toString() {
            return url();
        }
    }
    public static final VERSION DEFAULT_VERSION = VERSION.V1;
    public static final String CLOUD_MINE_URL = "https://api.cloudmine.me";
    public static final String APP = "/app";


    public CloudMineURLBuilder() {
        this(ApiCredentials.applicationIdentifier());
    }

    public CloudMineURLBuilder(String appId) {
        this(CLOUD_MINE_URL, appId);
    }

    protected CloudMineURLBuilder(String cloudMineUrl, String appId) {
        this(cloudMineUrl + DEFAULT_VERSION + APP + formatUrlPart(appId), "", "");
    }

    protected CloudMineURLBuilder(String baseUrl, String actions, String queryParams) {
        super(baseUrl, actions, queryParams);

    }

    protected CloudMineURLBuilder newBuilder(String baseUrl, String actions, String queryParams) {
        return new CloudMineURLBuilder(baseUrl, actions, queryParams);
    }


    public CloudMineURLBuilder data() {
        return addAction("data");
    }

    public CloudMineURLBuilder deleteAll() {
        return data().addQuery("all", "true");
    }

    public CloudMineURLBuilder text() {
        return this.addAction("text");
    }
}

package com.cloudmine.api.rest;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/16/12, 11:21 AM
 */
public class CloudMineURLBuilder extends URLBuilder {
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

    private final String appId;

    public CloudMineURLBuilder(String appId) {
        super(CLOUD_MINE_URL + DEFAULT_VERSION.url());
        this.appId = formatUrlPart(appId);
    }

    protected CloudMineURLBuilder(String cloudMineUrl, String appId) {
        super(cloudMineUrl + DEFAULT_VERSION.url());
        this.appId = formatUrlPart(appId);
    }

    public String text() {
        return url() + "/text";
    }

    @Override
    public String baseUrl() {
        return super.baseUrl() + APP + appId;
    }
}

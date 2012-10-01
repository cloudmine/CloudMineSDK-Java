package com.cloudmine.api.rest;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class SinglyURLBuilder extends BaseURLBuilder<SinglyURLBuilder> {

    private static final String BASE_URL = "https://api.singly.com";

    public SinglyURLBuilder() {
        this(BASE_URL, "", "");
    }

    protected SinglyURLBuilder(String baseUrl, String actions, String queryParams) {
        super(baseUrl, actions, queryParams);
    }

    @Override
    protected SinglyURLBuilder newBuilder(String baseUrl, String actions, String queryParams) {
        return new SinglyURLBuilder(baseUrl, actions, queryParams);
    }

    public SinglyURLBuilder oAuthorize() {
        return this.addAction("oauth").addAction("authorize");
    }

    public SinglyURLBuilder clientId(String clientId) {
        return this.addQuery("client_id", clientId);
    }

    public SinglyURLBuilder redirectTo(String uri) {
        return this.addQuery("redirect_uri", uri);
    }

    public SinglyURLBuilder service(CMSocial.Service service) {
        return this.addAction("services").addAction(service.asUrlString());
    }

    public SinglyURLBuilder services(CMSocial.Service service) {
        return addAction("v0").addAction("services").addAction(service.asUrlString());
    }

    public SinglyURLBuilder action(CMSocial.Action action) {
        return addAction(action.asUrlString());
    }

    public SinglyURLBuilder token(String token) {
        return addQuery("access_token", token);
    }
}

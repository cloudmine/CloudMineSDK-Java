package com.cloudmine.api.rest;

import com.cloudmine.api.CMUserToken;
import com.cloudmine.api.rest.callbacks.WebServiceCallback;
import com.cloudmine.api.rest.response.CMResponse;
import org.apache.http.Header;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicHeader;

import java.util.concurrent.Future;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/22/12, 10:54 AM
 */
public class UserCMWebService extends CMWebService {

    private static final String SESSION_TOKEN_HEADER_KEY = "X-CloudMine-SessionToken";


    protected final CMUserToken userToken;
    private final Header userHeader;


    /**
     * Provides access to a specific users data.
     * @param baseUrl the base URL to hit; this should be something like https://api.cloudmine.me/v1/app/{appid}/user
     * @param token the users token that represents a logged in session; acquire by using CMWebService.login
     * @param asynchronousHttpClient This should probably be the AndroidAsynchronousHttpClient, but you may provide your own implementation of the interface
     */
    UserCMWebService(CMURLBuilder baseUrl, CMUserToken token, AsynchronousHttpClient asynchronousHttpClient) {
        super(baseUrl, asynchronousHttpClient);
        this.userToken = token;
        userHeader = new BasicHeader(SESSION_TOKEN_HEADER_KEY, token.sessionToken());
    }

    public static UserCMWebService UserCMWebService(CMURLBuilder baseUrl, CMUserToken token, AsynchronousHttpClient asynchronousHttpClient) {
        return new UserCMWebService(baseUrl, token, asynchronousHttpClient);
    }


    @Override
    protected void addCloudMineHeader(AbstractHttpMessage message) {
        super.addCloudMineHeader(message);
        message.addHeader(userHeader);
    }

    public Future<CMResponse> asyncLogout() {
        return asyncLogout(WebServiceCallback.DO_NOTHING);
    }

    public Future<CMResponse> asyncLogout(WebServiceCallback callback) {
        return asyncLogout(userToken, callback);
    }

    @Override
    public UserCMWebService userWebService(CMUserToken token) {
        return this;
    }
}

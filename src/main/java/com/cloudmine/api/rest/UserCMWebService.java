package com.cloudmine.api.rest;

import com.cloudmine.api.CMSessionToken;
import com.cloudmine.api.rest.callbacks.WebServiceCallback;
import com.cloudmine.api.rest.response.CMResponse;
import org.apache.http.Header;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicHeader;

import java.util.concurrent.Future;

/**
 * A {@link CMWebService} that does all its operations at the user level
 * Copyright CloudMine LLC
 */
public class UserCMWebService extends CMWebService {

    private static final String SESSION_TOKEN_HEADER_KEY = "X-CloudMine-SessionToken";


    protected final CMSessionToken sessionToken;
    private final Header userHeader;


    /**
     * Provides access to a specific users data.
     * @param baseUrl the base URL to hit; this should be something like https://api.cloudmine.me/v1/app/{appid}/user
     * @param token the users token that represents a logged in session; acquire by using CMWebService.login
     * @param asynchronousHttpClient This should probably be the AndroidAsynchronousHttpClient, but you may provide your own implementation of the interface
     */
    UserCMWebService(CMURLBuilder baseUrl, CMSessionToken token, AsynchronousHttpClient asynchronousHttpClient) {
        super(baseUrl, asynchronousHttpClient);
        this.sessionToken = token;
        userHeader = new BasicHeader(SESSION_TOKEN_HEADER_KEY, token.sessionToken());
    }

    /**
     * Instantiate a new UserCMWebService with the specified properties. You probably shouldn't be calling this; instead, use
     * {@link CMWebService#userWebService(com.cloudmine.api.CMSessionToken)}
     * @param baseUrl the entire path, including the user part, for this store to operate on
     * @param token the logged in users token
     * @param asynchronousHttpClient the client to make asynchronous calls through; should be platform specific
     * @return a new instance of UserCMWebService
     */
    public static UserCMWebService UserCMWebService(CMURLBuilder baseUrl, CMSessionToken token, AsynchronousHttpClient asynchronousHttpClient) {
        return new UserCMWebService(baseUrl, token, asynchronousHttpClient);
    }


    @Override
    protected void addCloudMineHeader(AbstractHttpMessage message) {
        super.addCloudMineHeader(message);
        message.addHeader(userHeader);
    }

    /**
     * Log the user associated with this service out of the system. Any future calls to any methods will fail for lack of
     * authentication
     * @return A Future containing the response to the log out request
     */
    public Future<CMResponse> asyncLogout() {
        return asyncLogout(WebServiceCallback.DO_NOTHING);
    }

    /**
     * Log the user associated with this service out of the system. Any future calls to any methods will fail for lack of
     * authentication
     * @param callback a {@link WebServiceCallback} that expects a {@link CMResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.CMResponseCallback} is passed in
     * @return A Future containing the response to the log out request
     */
    public Future<CMResponse> asyncLogout(WebServiceCallback callback) {
        return asyncLogout(sessionToken, callback);
    }

    @Override
    public UserCMWebService userWebService(CMSessionToken token) {
        return this;
    }
}

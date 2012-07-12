package com.cloudmine.api.rest;

import com.cloudmine.api.CMSessionToken;
import com.cloudmine.api.CMUser;
import com.cloudmine.api.rest.callbacks.Callback;
import com.cloudmine.api.rest.response.CMResponse;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicHeader;

/**
 * A {@link CMWebService} that does all its operations at the user level
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
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
        userHeader = createSessionToken(token);
    }

    private BasicHeader createSessionToken(CMSessionToken token) {
        return new BasicHeader(SESSION_TOKEN_HEADER_KEY, token.getSessionToken());
    }

    /**
     * Instantiate a new UserCMWebService with the specified properties. You probably shouldn't be calling this; instead, use
     * {@link CMWebService#getUserWebService(com.cloudmine.api.CMSessionToken)}
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
     */
    public void asyncLogout() {
        asyncLogout(Callback.DO_NOTHING);
    }

    /**
     * Log the user associated with this service out of the system. Any future calls to any methods will fail for lack of
     * authentication
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects a {@link CMResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.CMResponseCallback} is passed in
     */
    public void asyncLogout(Callback callback) {
        asyncLogout(sessionToken, callback);
    }

    /**
     *
     * @param callback expects a {@link com.cloudmine.api.rest.response.CMObjectResponse}, recommended that {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used
     */
    public void asyncLoadLoggedInUserProfile(Callback callback) {
        HttpGet get = createGet(baseUrl.account().mine().asUrlString());
        executeAsyncCommand(get, callback, cmObjectResponseConstructor());
    }

    public void asyncInsertUserProfile(CMUser user, Callback callback) {
        HttpPut put = createProfilePut(user);
        executeAsyncCommand(put, callback, creationResponseConstructor());
    }

    @Override
    public UserCMWebService getUserWebService(CMSessionToken token) {
        return this;
    }

    private HttpPut createProfilePut(CMUser user) {
        HttpPut put = new HttpPut(baseUrl.account().asUrlString());
        addCloudMineHeader(put);
        addJson(put, user.profileTransportRepresentation());
        return put;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        UserCMWebService that = (UserCMWebService) o;

        if (sessionToken != null ? !sessionToken.equals(that.sessionToken) : that.sessionToken != null) return false;
        if (userHeader != null ? !userHeader.equals(that.userHeader) : that.userHeader != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (sessionToken != null ? sessionToken.hashCode() : 0);
        result = 31 * result + (userHeader != null ? userHeader.hashCode() : 0);
        return result;
    }
}

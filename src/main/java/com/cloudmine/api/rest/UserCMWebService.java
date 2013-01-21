package com.cloudmine.api.rest;

import com.cloudmine.api.*;
import com.cloudmine.api.exceptions.InvalidRequestException;
import com.cloudmine.api.rest.callbacks.CMCallback;
import com.cloudmine.api.rest.callbacks.Callback;
import com.cloudmine.api.rest.response.CMObjectResponse;
import com.cloudmine.api.rest.response.CMResponse;
import com.cloudmine.api.rest.response.CreationResponse;
import com.cloudmine.api.rest.response.SocialGraphResponse;
import org.apache.http.Header;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.AbstractHttpMessage;

import java.util.HashMap;

/**
 * A {@link CMWebService} that does all its operations at the user level
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class UserCMWebService extends CMWebService {


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
        userHeader = LibrarySpecificClassCreator.getCreator().getHeaderFactory().getUserCloudMineHeader(token);
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
        asyncLogout(CMCallback.<CMResponse>doNothing());
    }

    /**
     * Log the user associated with this service out of the system. Any future calls to any methods will fail for lack of
     * authentication
     * @param callback a {@link com.cloudmine.api.rest.callbacks.Callback} that expects a {@link CMResponse} or a parent class. It is recommended an {@link com.cloudmine.api.rest.callbacks.CMResponseCallback} is passed in
     */
    public void asyncLogout(Callback<CMResponse> callback) {
        asyncLogout(sessionToken, callback);
    }

    /**
     * Create or save the given CMAccessList
     * @param list
     * @param callback expects a {@link com.cloudmine.api.rest.response.CreationResponse}, recommended that you use a {@link com.cloudmine.api.rest.callbacks.CreationResponseCallback}
     */
    public void asyncInsert(CMAccessList list, Callback<CreationResponse> callback) {
        executeAsyncCommand(createAccessListPost(list), callback, creationResponseConstructor());
    }

    /**
     *
     * @param callback expects a {@link com.cloudmine.api.rest.response.CMObjectResponse}, recommended that {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used
     */
    public void asyncLoadLoggedInUserProfile(Callback<CMObjectResponse> callback) {
        HttpGet get = createGet(baseUrl.account().mine().asUrlString());
        executeAsyncCommand(get, callback, cmObjectResponseConstructor());
    }

    /**
     * Update a user's profile. The user must be logged in for this to work
     * @param user the users profile to update; note that the user associated with this UserCMWebService will always be the one updated, even if the passed in user is different
     * @param callback callback that expects a {@link com.cloudmine.api.rest.response.CreationResponse}. It is recommended that a {@link com.cloudmine.api.rest.callbacks.CreationResponseCallback}
     */
    public void asyncInsertUserProfile(CMUser user, Callback<CreationResponse> callback) {
        HttpPut put = createProfilePut(user);
        executeAsyncCommand(put, callback, creationResponseConstructor());
    }

    /**
     * Load the access lists belonging to the user associated with this object
     * @param callback expects a {@link com.cloudmine.api.rest.response.CMObjectResponse}, it is recommended that a {@link com.cloudmine.api.rest.callbacks.CMObjectResponseCallback} is used here
     */
    public void asyncLoadAccessLists(Callback<CMObjectResponse> callback) {
        HttpGet get = createGet(baseUrl.access().asUrlString());
        executeAsyncCommand(get, callback, cmObjectResponseConstructor());
    }

    /**
     * A call to execute the Query on the Given Social Network. Returns the response body in the Callback.
     *
     * @param service The CMSocial.Service service you
     * @param httpVerb The {@link com.cloudmine.api.rest.HttpVerb} for the request.
     * @param baseQuery The base query for the URL. Can be an empty string.
     * @param parameters The Parameters which will be added to the request. For example: new HashMap<String, Object>() {{ put("Param1", "Value"); }} Can be null.
     * @param headers The headers which will be added to the request. For example: new HashMap<String, Object>() {{ put("Content-Type", "application/octet-stream"); }} Can be null.
     * @param data The data which will put into the HTTP body of the request. Can be null.
     * @param callback A {@link com.cloudmine.api.rest.callbacks.CMSocialLoginResponseCallback which has a {@link com.cloudmine.api.rest.response.CMSocialLoginResponse}.
     * @throws InvalidRequestException A {@link com.cloudmine.api.exceptions.InvalidRequestException} is thrown if you do not use an appropriate HTTP verb. Note that PATCH is not support as of now.
     */
    public void asyncSocialGraphQueryOnNetwork(CMSocial.Service service,
                                               HttpVerb httpVerb,
                                               String baseQuery,
                                               HashMap<String, Object> parameters,
                                               HashMap<String, Object> headers,
                                               ByteArrayEntity data,
                                               Callback<SocialGraphResponse> callback) throws InvalidRequestException {

        CMURLBuilder url = baseUrl.social().addKey(service.asUrlString()).addKey(baseQuery);

        if (parameters != null)
            url = url.addQuery("params", CMURLBuilder.encode(JsonUtilities.mapToJson(parameters)));

        if (headers != null)
            url = url.addQuery("headers", CMURLBuilder.encode(JsonUtilities.mapToJson(headers)));

        String finalURL = url.asUrlString();
        boolean canHaveData = false;
        HttpRequestBase request = null;

        switch (httpVerb) {
            case GET:
                request = new HttpGet(finalURL);
                break;
            case POST:
                canHaveData = true;
                request = new HttpPost(finalURL);
                break;
            case PUT:
                canHaveData = true;
                request = new HttpPut(finalURL);
                break;
            case DELETE:
                request = new HttpDelete(finalURL);
                break;
            case PATCH:
            default:
                throw new InvalidRequestException("*** CloudMine Error! Unsupported Type" + httpVerb);
        }

        addCloudMineHeader(request);

        if (data != null && canHaveData)
            ((HttpEntityEnclosingRequestBase)request).setEntity(data);

        executeAsyncCommand(request, callback, SocialGraphResponse.CONSTRUCTOR);
    }

    @Override
    public UserCMWebService getUserWebService(CMSessionToken token) {
        return this;
    }

    private HttpPost createAccessListPost(CMAccessList list) {
        HttpPost post = new HttpPost(baseUrl.access().asUrlString());
        addCloudMineHeader(post);
        addJson(post, list.transportableRepresentation());
        return post;
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

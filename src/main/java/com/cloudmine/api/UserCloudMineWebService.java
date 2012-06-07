package com.cloudmine.api;

import android.os.Parcel;
import android.os.Parcelable;
import com.cloudmine.api.rest.*;
import com.cloudmine.api.rest.callbacks.WebServiceCallback;
import com.cloudmine.api.rest.response.CloudMineResponse;
import org.apache.http.Header;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicHeader;

import java.util.concurrent.Future;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/22/12, 10:54 AM
 */
public class UserCloudMineWebService extends CloudMineWebService implements Parcelable {

    private static final String SESSION_TOKEN_HEADER_KEY = "X-CloudMine-SessionToken";
    public static final Creator<UserCloudMineWebService> CREATOR =
            new Creator<UserCloudMineWebService>() {
                @Override
                public UserCloudMineWebService createFromParcel(Parcel parcel) {
                    return new UserCloudMineWebService(parcel);
                }

                @Override
                public UserCloudMineWebService[] newArray(int i) {
                    return new UserCloudMineWebService[i];
                }
            };

    private final UserToken userToken;
    private final Header userHeader;

    public UserCloudMineWebService(String appId, UserToken token) {
        this(new CloudMineURLBuilder(appId).user(), token, new AndroidAsynchronousHttpClient());
    }

    public UserCloudMineWebService(CloudMineURLBuilder baseUrl, UserToken token, AsynchronousHttpClient asynchronousHttpClient) {
        super(baseUrl, asynchronousHttpClient);
        this.userToken = token;
        userHeader = new BasicHeader(SESSION_TOKEN_HEADER_KEY, token.sessionToken());
    }

    public UserCloudMineWebService(Parcel in) {
        this(new CloudMineURLBuilder(in.readString()), new UserToken(in.readString()), new AndroidAsynchronousHttpClient());
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(userToken.asJson());
    }

    @Override
    public void addCloudMineHeader(AbstractHttpMessage message) {
        super.addCloudMineHeader(message);
        message.addHeader(userHeader);
    }

    public Future<CloudMineResponse> asyncLogout() {
        return asyncLogout(WebServiceCallback.DO_NOTHING);
    }

    public Future<CloudMineResponse> asyncLogout(WebServiceCallback callback) {
        return asyncLogout(userToken, callback);
    }

    @Override
    public UserCloudMineWebService userWebService(UserToken token) {
        return this;
    }
}

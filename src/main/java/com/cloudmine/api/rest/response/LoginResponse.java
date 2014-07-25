package com.cloudmine.api.rest.response;

import com.cloudmine.api.CMSessionToken;
import com.cloudmine.api.JavaCMUser;
import com.cloudmine.api.exceptions.ConversionException;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.response.code.LoginCode;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Returned by the CloudMine service in response to log in requests. Includes the sessionToken used by
 * services that operate at the user level.
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class LoginResponse extends ResponseBase<LoginCode> {
    private static final Logger LOG = LoggerFactory.getLogger(LoginResponse.class);
    public static final ResponseConstructor<LoginResponse> CONSTRUCTOR = new ResponseConstructor<LoginResponse>() {
        @Override
        public LoginResponse construct(HttpResponse response) {
            return new LoginResponse(response);
        }

        @Override
        public LoginResponse construct(String messageBody, int responseCode) throws CreationException {
            return new LoginResponse(messageBody, responseCode);
        }
    };

    private final CMSessionToken sessionToken;

    /**
     * Instantiate a new LoginResponse. You should probably not be calling this yourself
     * @param response a response to a log in request
     */
    public LoginResponse(HttpResponse response) {
        super(response);
        sessionToken = readInToken();
    }

    public LoginResponse(String json) {
        this(json, 200);
    }

    /**
     * Internal use only
     * @param json
     * @param responseCode
     */
    public LoginResponse(String json, int responseCode) {
        super(json, responseCode);
        sessionToken = readInToken();
    }


    public LoginCode getResponseCode() {
        return LoginCode.codeForStatus(getStatusCode());
    }

    public String getProfileTransportRepresentation() {
        Object profile = getObject(JavaCMUser.PROFILE_KEY);
        if(profile instanceof Map) {
            return JsonUtilities.mapToJson((Map<String, ? extends Object>) profile);
        }
        return JsonUtilities.EMPTY_JSON;
    }


    public <T extends JavaCMUser> T getUserObject(Class<T> userClass) {
        T user = JsonUtilities.jsonToClass(getProfileTransportRepresentation(), userClass);
        if(user != null) {
            user.setSessionToken(sessionToken);
        }
        return user;
    }

    /**
     * the token used to authenticate this session with the server. If the request failed, it will be equal to {@link com.cloudmine.api.CMSessionToken#FAILED}
     * @return the token used to authenticate this session with the server
     */
    public CMSessionToken getSessionToken() {
        return sessionToken;
    }

    private CMSessionToken readInToken() {
        CMSessionToken tempToken;
        if(wasSuccess()) {
            try {
                tempToken = new CMSessionToken(transportableRepresentation());
            } catch (ConversionException e) {
                LOG.error("Unable to parse json", e);
                tempToken = CMSessionToken.FAILED;
            }
        } else {
            tempToken = CMSessionToken.FAILED;
        }
        return tempToken;
    }
}

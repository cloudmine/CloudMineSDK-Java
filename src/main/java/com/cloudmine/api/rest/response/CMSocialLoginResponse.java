package com.cloudmine.api.rest.response;

import com.cloudmine.api.CMSessionToken;
import com.cloudmine.api.JavaCMUser;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.response.code.CMSocialCode;
import org.apache.http.HttpResponse;

import java.util.Map;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMSocialLoginResponse extends ResponseBase<CMSocialCode>{
    public static final ResponseConstructor<CMSocialLoginResponse> CONSTRUCTOR = new ResponseConstructor<CMSocialLoginResponse>() {
        @Override
        public CMSocialLoginResponse construct(HttpResponse response) throws CreationException {
            return new CMSocialLoginResponse(response);
        }

        @Override
        public CMSocialLoginResponse construct(String messageBody, int responseCode) throws CreationException {
            return new CMSocialLoginResponse(messageBody, responseCode);
        }
    };

    private final CMSessionToken token;
    private final JavaCMUser user;

    public CMSocialLoginResponse(HttpResponse response) {
        super(response);
        token = new CMSessionToken(getMessageBody());
        Object profileObject = JsonUtilities.jsonToMap(getMessageBody()).get(JavaCMUser.PROFILE_KEY);
        if(!(profileObject instanceof Map)) {
            user = null;
            return;
        }
        String profile = JsonUtilities.mapToJson((Map<String, ? extends Object>) profileObject);
        user = (JavaCMUser)JsonUtilities.jsonToClass(profile);
        user.setSessionToken(token);
    }

    public CMSocialLoginResponse(String msgBody, int responseCode) {
        super(msgBody, responseCode);
        token = new CMSessionToken(msgBody);
        Object profileObject = JsonUtilities.jsonToMap(msgBody).get(JavaCMUser.PROFILE_KEY);
        if(!(profileObject instanceof Map)) {
            user = null;
            return;
        }
        String profile = JsonUtilities.mapToJson((Map<String, ? extends Object>) profileObject);
        user = (JavaCMUser)JsonUtilities.jsonToClass(profile);
        user.setSessionToken(token);
    }

    @Override
    public CMSocialCode getResponseCode() {
        return CMSocialCode.codeForStatus(getStatusCode());
    }

    public CMSessionToken getSessionToken() {
        return token;
    }

    /**
     * May be null if the login did not succeed.
     * @return the user profile for the logged in user
     */
    public JavaCMUser getUser() {
        return user;
    }


}

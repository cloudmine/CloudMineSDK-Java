package com.cloudmine.api.rest.response;

import com.cloudmine.api.JavaCMUser;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.JsonUtilities;
import org.apache.http.HttpResponse;

import java.util.Map;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMSocialLoginResponse extends LoginResponse{
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

    private final JavaCMUser user;

    public CMSocialLoginResponse(HttpResponse response) {
        super(response);

        String messageBody = getMessageBody();
        user = getUserFromMessageBody(messageBody);
    }

    public CMSocialLoginResponse(String msgBody, int responseCode) {
        super(msgBody, responseCode);
        user = getUserFromMessageBody(msgBody);
    }


    protected JavaCMUser getUserFromMessageBody(String messageBody) {
        Object profileObject = JsonUtilities.jsonToMap(messageBody).get(JavaCMUser.PROFILE_KEY);
        if(!(profileObject instanceof Map)) {
            return null;
        }
        String profile = JsonUtilities.mapToJson((Map<String, ? extends Object>) profileObject);
        JavaCMUser user = (JavaCMUser)JsonUtilities.jsonToClass(profile);
        user.setSessionToken(getSessionToken());
        return user;
    }

    /**
     * May be null if the login did not succeed.
     * @return the user profile for the logged in user
     */
    public JavaCMUser getUser() {
        return user;
    }


}

package com.cloudmine.api.rest.response;

import com.cloudmine.api.CMSessionToken;
import com.cloudmine.api.CMUser;
import com.cloudmine.api.rest.JsonUtilities;

import java.util.Map;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMSocialLoginResponse {

    private final CMSessionToken token;
    private final CMUser user;

    public CMSocialLoginResponse(String msgBody, int responseCode) {
        token = new CMSessionToken(msgBody);
        Object profileObject = JsonUtilities.jsonToMap(msgBody).get(CMUser.PROFILE_KEY);
        if(!(profileObject instanceof Map)) {
            user = null;
            return;
        }
        String profile = JsonUtilities.mapToJson((Map<String, ? extends Object>) profileObject);
        user = (CMUser)JsonUtilities.jsonToClass(profile);
        user.setSessionToken(token);
    }

    public CMSessionToken getSessionToken() {
        return token;
    }

    public CMUser getUser() {
        return user;
    }
}

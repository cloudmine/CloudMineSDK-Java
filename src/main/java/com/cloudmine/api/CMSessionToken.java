package com.cloudmine.api;

import com.cloudmine.api.exceptions.JsonConversionException;
import com.cloudmine.api.rest.Json;
import com.cloudmine.api.rest.JsonUtilities;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a logged in user session. Session tokens expire after two weeks of non-use. Creating a new session token will not invalidate old sessions; sessions are only invalidated if they expire or if the user logs out.
 * For more information, see <a href="https://cloudmine.me/docs/user-management#ref/account_login">the CloudMine documentation</a>
 * Obtained by calling login, either directly on a {@link CMUser} object, or by passing a CMUser into {@link com.cloudmine.api.rest.CMStore#login(CMUser)} or {@link com.cloudmine.api.rest.CMWebService#asyncLogin(CMUser)}
 * Copyright CloudMine LLC
 */
public class CMSessionToken implements Json {
    private static final Date EXPIRED_DATE = new Date(0);
    public static final String INVALID_TOKEN = "invalidToken";
    /**
     * Represents a failed log in attempt. Will be returned by {@link com.cloudmine.api.rest.response.LoginResponse#userToken()} if the log in request fails
     */
    public static final CMSessionToken FAILED = CMSessionToken.CMSessionToken(INVALID_TOKEN, EXPIRED_DATE);

    public static final String SESSION_KEY = "session_token";
    public static final String EXPIRES_KEY = "expires";
    private final String sessionToken;
    private final DateTime expires;

    private CMSessionToken(String json) throws JsonConversionException {
        boolean jsonIsEmpty = json == null || "null".equals(json) || "".equals(json);
        if(jsonIsEmpty) {
            sessionToken = INVALID_TOKEN;
            expires = new DateTime(EXPIRED_DATE);
        } else {
            Map<String, Object> objectMap = JsonUtilities.jsonToMap(json);
            boolean isMissingKey = !objectMap.containsKey(SESSION_KEY) ||
                    !objectMap.containsKey(EXPIRES_KEY);
            if(isMissingKey) {
                throw new JsonConversionException("Can't create CMSessionToken from json missing field");
            }
            sessionToken = objectMap.get(SESSION_KEY).toString();
    //        String dateString = objectMap.get(EXPIRES_KEY).toString();
    //        expires = JsonUtilities.toDate(dateString);
            expires = new DateTime(); //TODO figure out how we're going to be formatting these dates
        }
    }

    private CMSessionToken(String sessionToken, Date expires) {
        this.sessionToken = sessionToken;
        this.expires = new DateTime(expires);
    }

    /**
     * Instantiates a new CMSessionToken based on a JSON string returned from a login request
     * @param json A JSON string returned from a login request
     * @return a new CMSessionToken
     * @throws JsonConversionException if invalid JSON is passed in
     */
    public static CMSessionToken CMSessionToken(String json) throws JsonConversionException {
        return new CMSessionToken(json);
    }

    /**
     * Instantiate a new CMSessionToken based on a session token and an expires date.
     * @param sessionToken the value for SESSION_KEY in a log in response
     * @param expires the value for EXPIRES_KEY in a log in response
     * @return a new CMSessionToken
     */
    public static CMSessionToken CMSessionToken(String sessionToken, Date expires) {
        return new CMSessionToken(sessionToken, expires);
    }

    public String asJson() throws JsonConversionException {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put(SESSION_KEY, sessionToken);
        jsonMap.put(EXPIRES_KEY, expires.toDate());
        return JsonUtilities.mapToJson(jsonMap);
    }

    /**
     * Get the session token String
     * @return the session token string
     */
    public String sessionToken() {
        return sessionToken;
    }

    /**
     * Whether this token has expired or not.
     * @return false if it has expired, true otherwise
     */
    public boolean isValid() {
        return expires.isAfterNow();
    }

}

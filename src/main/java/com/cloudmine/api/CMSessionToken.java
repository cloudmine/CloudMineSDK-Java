package com.cloudmine.api;

import com.cloudmine.api.exceptions.JsonConversionException;
import com.cloudmine.api.rest.Json;
import com.cloudmine.api.rest.JsonUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a logged in user session. Session tokens expire after two weeks of non-use. Creating a new session token will not invalidate old sessions; sessions are only invalidated if they expire or if the user logs out.
 * For more information, see <a href="https://cloudmine.me/docs/user-management#account_login">the CloudMine documentation</a>
 * Obtained by calling login, either directly on a {@link CMUser} object, or by passing a CMUser into {@link com.cloudmine.api.rest.CMStore#login(CMUser)} or {@link com.cloudmine.api.rest.CMWebService#asyncLogin(CMUser)}
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class CMSessionToken implements Json {
    private static final Logger LOG = LoggerFactory.getLogger(CMSessionToken.class);
    private static final DateFormat LOGIN_EXPIRES_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
    private static final Date EXPIRED_DATE = new Date(0);
    public static final String INVALID_TOKEN = "invalidToken";
    /**
     * Represents a failed log in attempt. Will be returned by {@link com.cloudmine.api.rest.response.LoginResponse#getSessionToken()} if the log in request fails
     */
    public static final CMSessionToken FAILED = CMSessionToken.CMSessionToken(INVALID_TOKEN, EXPIRED_DATE);

    public static final String SESSION_KEY = "session_token";
    public static final String EXPIRES_KEY = "expires";
    private final String sessionToken;
    private final Date expires;

    /**
     * Instantiates a new CMSessionToken based on a JSON string returned from a login request
     * @param json A JSON string returned from a login request
     * @return a new CMSessionToken
     * @throws JsonConversionException if invalid JSON is passed in
     */
    public static CMSessionToken CMSessionToken(String json) throws JsonConversionException {
        return new CMSessionToken(json);
    }

    private CMSessionToken(String json) throws JsonConversionException {
        boolean jsonIsEmpty = json == null || "null".equals(json) || "".equals(json);
        if(jsonIsEmpty) {
            sessionToken = INVALID_TOKEN;
            expires = EXPIRED_DATE;
        } else {
            Map<String, Object> objectMap = JsonUtilities.jsonToMap(json);
            boolean isMissingKey = !objectMap.containsKey(SESSION_KEY) ||
                    !objectMap.containsKey(EXPIRES_KEY);
            if(isMissingKey) {
                throw new JsonConversionException("Can't create CMSessionToken from json missing field");
            }
            sessionToken = objectMap.get(SESSION_KEY).toString();
            Object dateObject = objectMap.get(EXPIRES_KEY);
            Date tempDate;
            if(dateObject instanceof Date) {
                tempDate = (Date) dateObject;
            } else if(dateObject != null) {
                String dateString = dateObject.toString();
                try {
                    tempDate = LOGIN_EXPIRES_FORMAT.parse(dateString);
                } catch (ParseException e) {
                    throw new JsonConversionException(e);
                }
            } else {
                tempDate = EXPIRED_DATE;
            }
            expires = tempDate;
        }
    }

    private CMSessionToken(String sessionToken, Date expires) {
        this.sessionToken = sessionToken;
        this.expires = expires;
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
        jsonMap.put(EXPIRES_KEY, expires);
        return JsonUtilities.mapToJson(jsonMap);
    }

    /**
     * Get the session token String
     * @return the session token string
     */
    public String getSessionToken() {
        return sessionToken;
    }

    public Date getExpiredDate() {
        return expires;
    }

    /**
     * Whether this token has expired or not.
     * @return false if it has expired, true otherwise
     */
    public boolean isValid() {
        return FAILED != this &&
                expires.after(new Date());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CMSessionToken that = (CMSessionToken) o;

        if (expires != null ? !expires.equals(that.expires) : that.expires != null) return false;
        if (sessionToken != null ? !sessionToken.equals(that.sessionToken) : that.sessionToken != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sessionToken != null ? sessionToken.hashCode() : 0;
        result = 31 * result + (expires != null ? expires.hashCode() : 0);
        return result;
    }
}

package com.cloudmine.api;

import com.cloudmine.api.exceptions.ConversionException;
import com.cloudmine.api.rest.Transportable;
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
public class CMSessionToken implements Transportable {
    private static final Logger LOG = LoggerFactory.getLogger(CMSessionToken.class);
    private static final DateFormat LOGIN_EXPIRES_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
    private static final Date EXPIRED_DATE = new Date(0);
    public static final String INVALID_TOKEN = "invalidToken";
    /**
     * Represents a failed log in attempt. Will be returned by {@link com.cloudmine.api.rest.response.LoginResponse#getSessionToken()} if the log in request fails
     */
    public static final CMSessionToken FAILED = new CMSessionToken(INVALID_TOKEN, EXPIRED_DATE);

    public static final String SESSION_KEY = "session_token";
    public static final String EXPIRES_KEY = "expires";
    private final String sessionToken;
    private final Date expires;

    /**
     * Instantiates a new CMSessionToken based on a transportable string returned from a login request. If given
     * an invalid transport string, the CMSessionToken may be equal to FAILED
     * @param transportString A transport string returned from a login request
     * @return a new CMSessionToken
     * @throws ConversionException if invalid transport representation is passed in
     */
    public CMSessionToken(String transportString) throws ConversionException {
        boolean jsonIsEmpty = transportString == null || "null".equals(transportString) || "".equals(transportString);
        if(jsonIsEmpty) {
            sessionToken = INVALID_TOKEN;
            expires = EXPIRED_DATE;
        } else {
            Map<String, Object> objectMap = JsonUtilities.jsonToMap(transportString);
            boolean isMissingKey = !objectMap.containsKey(SESSION_KEY) ||
                    !objectMap.containsKey(EXPIRES_KEY);
            if(isMissingKey) {
                sessionToken = INVALID_TOKEN;
                expires = EXPIRED_DATE;
            } else {
                sessionToken = objectMap.get(SESSION_KEY).toString();
                expires = getExpiresDate(objectMap);
            }
        }
    }

    private Date getExpiresDate(Map<String, Object> objectMap) {
        Object dateObject = objectMap.get(EXPIRES_KEY);
        Date tempDate;
        if(dateObject instanceof Date) {
            tempDate = (Date) dateObject;
        } else if(dateObject != null) {
            String dateString = dateObject.toString();
            try {
                tempDate = LOGIN_EXPIRES_FORMAT.parse(dateString);
            } catch (ParseException e) {
                throw new ConversionException(e);
            }
        } else {
            tempDate = EXPIRED_DATE;
        }
        return tempDate;
    }

    public CMSessionToken(String sessionToken, Date expires) {
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

    public String transportableRepresentation() throws ConversionException {
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

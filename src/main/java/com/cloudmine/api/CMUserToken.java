package com.cloudmine.api;

import com.cloudmine.api.exceptions.JsonConversionException;
import com.cloudmine.api.rest.Json;
import com.cloudmine.api.rest.JsonUtilities;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/21/12, 3:40 PM
 */
public class CMUserToken implements Json {
    private static final Date EXPIRED_DATE = new Date(0);
    public static final CMUserToken FAILED = CMUserToken("invalidToken", EXPIRED_DATE);

    public static final String SESSION_KEY = "session_token";
    public static final String EXPIRES_KEY = "expires";
    private final String sessionToken;
    private final DateTime expires;

    private CMUserToken(String json) {
        boolean jsonIsEmpty = json == null || "null".equals(json) || "".equals(json);
        if(jsonIsEmpty) {
            sessionToken = "";
            expires = new DateTime().minus(1);
        } else {
            Map<String, Object> objectMap = JsonUtilities.jsonToMap(json);
            boolean isMissingKey = !objectMap.containsKey(SESSION_KEY) ||
                    !objectMap.containsKey(EXPIRES_KEY);
            if(isMissingKey) {
                throw new JsonConversionException("Can't create CMUserToken from json missing field");
            }
            sessionToken = objectMap.get(SESSION_KEY).toString();
    //        String dateString = objectMap.get(EXPIRES_KEY).toString();
    //        expires = JsonUtilities.toDate(dateString);
            expires = new DateTime(); //TODO figure out how we're going to be formatting these dates
        }
    }

    private CMUserToken(String sessionToken, Date expires) {
        this.sessionToken = sessionToken;
        this.expires = new DateTime(expires);
    }

    public static CMUserToken CMUserToken(String json) {
        return new CMUserToken(json);
    }

    public static CMUserToken CMUserToken(String sessionToken, Date expires) {
        return new CMUserToken(sessionToken, expires);
    }

    public String asJson() {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put(SESSION_KEY, sessionToken);
        jsonMap.put(EXPIRES_KEY, expires.toDate());
        return JsonUtilities.mapToJson(jsonMap);
    }

    public String sessionToken() {
        return sessionToken;
    }

    public boolean isValid() {
        return expires.isAfterNow();
    }

}
package com.cloudmine.api;

import com.cloudmine.api.rest.JsonUtilities;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/21/12, 11:40 AM
 */
public class User {

    public static final String EMAIL_KEY = "email";
    public static final String PASSWORD_KEY = "password";
    private final String email;
    private final String password;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String asJson() {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put(EMAIL_KEY, email);
        jsonMap.put(PASSWORD_KEY, password);
        return JsonUtilities.mapToJson(jsonMap);
    }

    public String encode() {
        String userString = email + ":" + password;
        return javax.xml.bind.DatatypeConverter.printBase64Binary(userString.getBytes());
    }

    public String toString() {
        return email + ":" + password;
    }


}

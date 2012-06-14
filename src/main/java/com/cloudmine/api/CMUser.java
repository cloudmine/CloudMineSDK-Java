package com.cloudmine.api;

import com.cloudmine.api.rest.JsonUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/21/12, 11:40 AM
 */
public class CMUser {
    private static final Logger LOG = LoggerFactory.getLogger(CMUser.class);
    public static final String EMAIL_KEY = "email";
    public static final String PASSWORD_KEY = "password";
    private final String email;
    private final String password;

    public static CMUser CMUser(String email, String password) {
        return new CMUser(email, password);
    }

    CMUser(String email, String password) {
        this.email = email;
        this.password = password;
    }


    public String asJson() {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put(EMAIL_KEY, email);
        jsonMap.put(PASSWORD_KEY, password);
        return JsonUtilities.mapToJson(jsonMap);
    }

    public String email() {
        return email;
    }

    public String password() {
        return password;
    }

    public String encode() {
        String userString = email + ":" + password;
        String encodedString = encodeString(userString);
        return encodedString;
    }

    protected String encodeString(String toEncode) {
        try {
            return javax.xml.bind.DatatypeConverter.printBase64Binary(toEncode.getBytes());
        }catch(NoClassDefFoundError ncdfe) {
            LOG.error("Do not instantiate CMUser objects on Android! You must use AndroidCMUser, as " +
                    "android does not provide an implementation for DataTypeConverter", ncdfe);
            throw ncdfe;
        }
    }

    public String toString() {
        return email + ":" + password;
    }


    public static void main(String... args) {
        CMUser user = CMUser("TA@t.com", "GOD");
        System.out.println(user.encode());
    }
}

package com.cloudmine.api.rest.response;

import com.cloudmine.api.rest.JsonUtilities;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class LoginResponseTest {

    @Test
    public void testGetProfileTransport() {
        String profileJson = "{" +
                "\"address\":\"123 Real St, Philadelphia, PA 19123\"," +
                "\"age\":50," +
                "\"paid\":false," +
                "\"__class__\":\"com.cloudmine.test.ExtendedCMUser\"," +
                "\"__type__\":\"user\",\"__id__\":\"7600e473c2ab4c918cb4e084628dd12a\"}";
        LoginResponse response = new LoginResponse("{\"session_token\":\"4a15024410b54d99858e964731dec007\"," +
                "\"expires\":\"Fri, 27 Jul 2012 20:26:43 GMT\"," +
                "\"profile\":" + profileJson + "}");

        assertTrue(JsonUtilities.isJsonEquivalent(profileJson, response.getProfileTransportRepresentation()));

    }
}

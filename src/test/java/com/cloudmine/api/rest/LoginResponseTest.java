package com.cloudmine.api.rest;

import com.cloudmine.api.rest.response.LoginResponse;
import com.cloudmine.test.ExtendedCMUser;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * <br>Copyright CloudMine LLC. All rights reserved
 * <br> See LICENSE file included with SDK for details.
 */
public class LoginResponseTest {

    @Test
    public void testGettingUser() {
        String responseJson = "{\"session_token\":\"4478e1f859814c77b4d7252af795b67e\",\"expires\":\"Wed, 07 May 2014 20:15:02 GMT\",\"profile\":" +
                "{\"userName\":null,\"address\":\"123 Real St, Philadelphia, PA 19123\",\"age\":50,\"paid\":false,\"userIdentifier\":\"frexxd@francis.com\",\"loginAttemptPossible\":false,\"__services__\":[],\"__class__\":\"com.cloudmine.test.ExtendedCMUser\",\"__type__\":\"user\",\"__id__\":\"d983831626014ca19bbda075853fb438\"}}";
        LoginResponse response = new LoginResponse(responseJson, 200);
        ExtendedCMUser extendedCMUser = response.getUserObject(ExtendedCMUser.class);
        assertEquals("123 Real St, Philadelphia, PA 19123", extendedCMUser.getAddress());
        assertEquals(50, extendedCMUser.getAge());
        assertEquals(false, extendedCMUser.isPaid());

        assertEquals("4478e1f859814c77b4d7252af795b67e", extendedCMUser.getSessionToken().getSessionToken());
    }
}

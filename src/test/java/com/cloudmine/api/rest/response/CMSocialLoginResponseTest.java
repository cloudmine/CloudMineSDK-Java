package com.cloudmine.api.rest.response;

import com.cloudmine.api.CMSessionToken;
import com.cloudmine.api.JavaCMUser;
import com.cloudmine.api.JavaCMUser;
import com.cloudmine.api.rest.CMSocial;
import org.junit.Test;

import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMSocialLoginResponseTest {

    @Test
    public void testSuccess() {
        String msgBody = "{\"session_token\":\"88f33732c85146c8ae638f5bd31b9405\",\"expires\":\"Tue, 23 Oct 2012 18:23:55 GMT\"," +
                "\"profile\":{\"__type__\":\"user\",\"__id__\":\"e73ee79182b14a75a2fddd050c6e6bcf\",\"__services__\":[\"twitter\",\"github\"]}}";
        CMSocialLoginResponse response = new CMSocialLoginResponse(msgBody, 200);

        CMSessionToken token = response.getSessionToken();
        assertTrue(token.isValid());

        JavaCMUser user = response.getUser();
        assertNotNull(user);
        assertTrue(user.isLoggedIn());

        Set<CMSocial.Service> services = user.getAuthenticatedServices();
        assertEquals(2, services.size());
        assertTrue(services.contains(CMSocial.Service.GITHUB));
        assertTrue(services.contains(CMSocial.Service.TWITTER));
    }
}

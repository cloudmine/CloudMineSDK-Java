package com.cloudmine.api.rest;

import com.cloudmine.api.CMApiCredentials;
import com.cloudmine.api.CMSessionToken;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * <br>Copyright CloudMine LLC. All rights reserved
 * <br> See LICENSE file included with SDK for details.
 */
public class CMWebServiceTest {




    @Test
    public void testConfigurableURL() {
        CMApiCredentials.initialize("id", "key", "https://api.rs.cloudmine.me");

        HttpGet get = CMWebService.getService().createGet();
        assertEquals("api.rs.cloudmine.me", get.getURI().getHost());

        CMWebService webService = CMWebService.getService("difId", "difKey", "https://verizon.api.cloudmine.me");
        get = webService.createGet();
        assertEquals("verizon.api.cloudmine.me", get.getURI().getHost());
        assertEquals("difKey", get.getFirstHeader(HeaderFactory.API_HEADER_KEY).getValue());

        CMApiCredentials.initialize("id", "key", "https://api.rs.cloudmine.me");
        assertEquals("difKey", webService.createGet().getFirstHeader(HeaderFactory.API_HEADER_KEY).getValue());

        UserCMWebService userCMWebService = webService.getUserWebService(new CMSessionToken("enouthenuteh", new Date()));
        get = userCMWebService.createGet();
        assertEquals("difKey", get.getFirstHeader(HeaderFactory.API_HEADER_KEY).getValue());
    }
}
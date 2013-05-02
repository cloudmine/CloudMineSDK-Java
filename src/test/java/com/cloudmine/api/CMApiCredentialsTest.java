package com.cloudmine.api;

import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.test.ServiceTestBase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 * User: johnmccarthy
 * Date: 6/12/12, 3:26 PM
 */
public class CMApiCredentialsTest extends ServiceTestBase {

    @Test
    public void testInitialize() {
        try {
            CMApiCredentials.initialize(null, "someApiKey");
            fail();
        } catch(CreationException ce) { /* expected */ }
        try {
            CMApiCredentials.initialize("someId", null);
            fail();
        } catch(CreationException ce) { /* expected */ }

        CMApiCredentials.initialize("someId", "someApiKey");

        assertEquals("someId", CMApiCredentials.getApplicationIdentifier());
        assertEquals("someApiKey", CMApiCredentials.getApplicationApiKey());


        try {
            CMApiCredentials.initialize("anotherId", "anotherApiKey");
            assertEquals("anotherId", CMApiCredentials.getApplicationIdentifier());
            assertEquals("anotherApiKey", CMApiCredentials.getApplicationApiKey());
        } catch(CreationException ce) {
            fail();
        }

        CMApiCredentials.initialize("someId", "someApiKey");
        assertEquals("someId", CMApiCredentials.getApplicationIdentifier());
        assertEquals("someApiKey", CMApiCredentials.getApplicationApiKey());
    }
}

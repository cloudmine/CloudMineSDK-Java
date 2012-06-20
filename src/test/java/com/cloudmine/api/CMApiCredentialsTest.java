package com.cloudmine.api;

import com.cloudmine.api.exceptions.CreationException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 6/12/12, 3:26 PM
 */
public class CMApiCredentialsTest {

    @Test
    public void testInitialize() {
        try {
            CMApiCredentials.initialize(null, "someApiKey");
        } catch(CreationException ce) { /* expected */ }
        try {
            CMApiCredentials.initialize("someId", null);
        } catch(CreationException ce) { /* expected */ }

        CMApiCredentials.initialize("someId", "someApiKey");

        assertEquals("someId", CMApiCredentials.getApplicationIdentifier());
        assertEquals("someApiKey", CMApiCredentials.getApplicationApiKey());


        try {
            CMApiCredentials.initialize("anotherId", "anotherApiKey");
            fail();
        } catch(CreationException ce) { /* expected */}

        CMApiCredentials.initialize("someId", "someApiKey"); // should be okay same values
    }
}

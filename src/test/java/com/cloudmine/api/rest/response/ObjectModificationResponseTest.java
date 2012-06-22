package com.cloudmine.api.rest.response;

import junit.framework.Assert;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 * CMUser: johnmccarthy
 * Date: 6/7/12, 3:24 PM
 */
public class ObjectModificationResponseTest {

    @Test
    public void testGetObjectCreationStatus() {
        ObjectModificationResponse response = new ObjectModificationResponse("{\n" +
                "    \"success\": {\n" +
                "      \"key1\": \"updated\",\n" +
                "      \"key2\": \"updated\",\n" +
                "      \"key3\": \"created\"\n" +
                "    }\n" +
                "}", 200);

        assertTrue(response.wasUpdated("key1"));
        assertFalse(response.wasUpdated("key3"));
        assertTrue(response.wasCreated("key3"));
        assertFalse(response.wasCreated("key2"));
        Assert.assertEquals(ResponseValue.UPDATED, response.getKeyResponse("key2"));
        Assert.assertEquals(ResponseValue.CREATED, response.getKeyResponse("key3"));
        Assert.assertEquals(ResponseValue.MISSING, response.getKeyResponse("key4"));
    }
}

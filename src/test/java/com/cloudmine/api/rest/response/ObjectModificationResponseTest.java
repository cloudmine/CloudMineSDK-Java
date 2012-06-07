package com.cloudmine.api.rest.response;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
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
        assertEquals(ResponseValue.UPDATED, response.keyResponse("key2"));
        assertEquals(ResponseValue.CREATED, response.keyResponse("key3"));
        assertEquals(ResponseValue.MISSING, response.keyResponse("key4"));
    }
}

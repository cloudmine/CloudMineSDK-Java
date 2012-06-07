package com.cloudmine.api.rest.response;

import com.cloudmine.api.SimpleCMObject;
import com.cloudmine.api.rest.response.CloudMineResponse;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/25/12, 6:41 PM
 */
public class CloudMineResponseTest {
    private static final String JSON_RESPONSE = "{\n" +
            "\"success\": {\n" +
            "\"xxThisWillNeedToBeGeneratedxx\": {\n" +
            "\"__class__\": \"task\",\n" +
            "\"taskName\": \"kglg\",\n" +
            "\"isDone\": false\n" +
            "},\n" +
            "\"someOtherKey\": {\n" +
            "\"__class__\": \"task\",\n" +
            "\"taskName\": \"kgsdfdsfsdflg\",\n" +
            "\"isDone\": true\n" +
            "}\n" +
            "},\n" +
            "\"errors\": {}\n" +
            "}";

    private static final String SET_RESPONSE = "{\n" +
            "    \"success\": {\n" +
            "      \"key1\": \"updated\",\n" +
            "      \"key2\": \"created\",\n" +
            "      \"key3\": \"created\"\n" +
            "    }\n" +
            "}";
    @Test
    public void testGetSuccessObjects() {
        CloudMineResponse response = new CloudMineResponse(JSON_RESPONSE, 200);

        Collection<SimpleCMObject> successObjects = response.getSuccessObjects();

        assertEquals(2, successObjects.size());
    }

    @Test
    public void successHasKey() {
        CloudMineResponse response = new CloudMineResponse(JSON_RESPONSE, 200);

        assertTrue(response.hasSuccess());
        assertTrue(response.hasSuccessKey("someOtherKey"));
        assertFalse(response.hasSuccessKey("taskName"));
    }
}

package com.cloudmine.api.rest.response;

import com.cloudmine.api.SimpleCMObject;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 * CMUser: johnmccarthy
 * Date: 6/7/12, 6:15 PM
 */
public class SuccessErrorResponseTest {

    private static final String SET_RESPONSE = "{\n" +
            "    \"success\": {\n" +
            "      \"key1\": \"updated\",\n" +
            "      \"key2\": \"created\",\n" +
            "      \"key3\": \"created\"\n" +
            "    }\n" +
            "}";
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



    @Test
    public void successHasKey() {
        SuccessErrorResponse response = new SuccessErrorResponse(JSON_RESPONSE, 200);

        assertTrue(response.hasSuccess());
        assertTrue(response.hasSuccessKey("someOtherKey"));
        assertFalse(response.hasSuccessKey("taskName"));
    }

    @Test
    public void testGetSuccessObjects() {
        SuccessErrorResponse response = new SuccessErrorResponse(SET_RESPONSE, 200);

        Collection<SimpleCMObject> successObjects = response.getSuccessObjects();

        assertEquals(3, successObjects.size());
    }
}

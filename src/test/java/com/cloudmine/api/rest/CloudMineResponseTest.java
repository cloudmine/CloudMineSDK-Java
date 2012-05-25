package com.cloudmine.api.rest;

import com.cloudmine.api.SimpleCMObject;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/25/12, 6:41 PM
 */
public class CloudMineResponseTest {
    private static final String jsonResponse = "{\n" +
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
    public void testGetSuccessObjects() {
        CloudMineResponse response = new CloudMineResponse(jsonResponse, 200);

        Collection<SimpleCMObject> successObjects = response.getSuccessObjects();

        assertEquals(2, successObjects.size());
    }
}

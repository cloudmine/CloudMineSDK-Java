package com.cloudmine.api.rest.response;

import com.cloudmine.api.CMType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CreationResponseTest {

    private static final String OBJECT_ID = "8bc73e529d6c418da480de7efa0971d9";
    private static final String SAMPLE_RESPONSE = "{\n" +
            "    \"__id__\": \"" + OBJECT_ID + "\",\n" +
            "    \"__type__\": \"user\",\n" +
            "    \"name\": \"Example User\",\n" +
            "    \"location\": {\n" +
            "         \"__type__\": \"geopoint\",\n" +
            "         \"longitude\": 45.5,\n" +
            "         \"latitude\": -70.2\n" +
            "    }\n" +
            "}";
    private static final CreationResponse RESPONSE = new CreationResponse(SAMPLE_RESPONSE, 201);

    @Test
    public void testGetObjectId() {
        assertEquals(OBJECT_ID, RESPONSE.getObjectId());
    }

    @Test
    public void testGetType() {
        assertEquals(CMType.USER, RESPONSE.getType());
    }
}

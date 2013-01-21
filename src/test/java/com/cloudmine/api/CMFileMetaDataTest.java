package com.cloudmine.api;

import com.cloudmine.api.rest.CMFileMetaData;
import com.cloudmine.api.rest.JsonUtilities;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMFileMetaDataTest {

    @Test
    public void testFromJson() {
        String json = "{\"content_type\":\"application/oop\",\"__id__\":\"1e210b64-5be7-4b8c-9844-01aadd628f23\",\"__type__\":\"file\",\"filename\":\"1e210b64-5be7-4b8c-9844-01aadd628f23\"}";

        CMFileMetaData data = JsonUtilities.jsonToClass(json, CMFileMetaData.class);
        assertNotNull(data);
        assertEquals("application/oop", data.getContentType());
    }
}

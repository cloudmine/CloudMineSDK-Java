package com.cloudmine.api.rest;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/16/12, 11:57 AM
 */
public class CloudMineURLBuilderTest {

    public static final String APP_ID = "taskList";

    @Test
    public void testGet() {
        CloudMineURLBuilder builder = new CloudMineURLBuilder(APP_ID);
        String expectedUrl = expectedBaseUrl();
        assertEquals(expectedUrl, builder.url());

        expectedUrl += "/text";
        assertEquals(expectedUrl, builder.text());
    }

    private String expectedBaseUrl() {
        return CloudMineURLBuilder.CLOUD_MINE_URL + CloudMineURLBuilder.DEFAULT_VERSION + CloudMineURLBuilder.APP + URL.SEPARATOR + APP_ID;
    }

}

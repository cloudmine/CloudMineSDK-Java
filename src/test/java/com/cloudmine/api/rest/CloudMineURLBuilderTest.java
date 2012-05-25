package com.cloudmine.api.rest;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

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
        assertEquals(expectedUrl, builder.urlString());

        expectedUrl += "/text";
        assertEquals(expectedUrl, builder.text().urlString());
    }

    @Test
    public void testSearch() {
        CloudMineURLBuilder builder = new CloudMineURLBuilder(APP_ID).search("[ingredients=\"chicken\"]");
        assertEquals(expectedBaseUrl() + "/search?q=%5Bingredients%3D%22chicken%22%5D", builder.urlString());
    }

    @Test
    public void testAccount() {
        CloudMineURLBuilder builder = new CloudMineURLBuilder(APP_ID).account();
        assertEquals(expectedBaseUrl() + "/account", builder.urlString());

        assertEquals(expectedBaseUrl() + "/account/create", builder.create().urlString());
    }

    @Test
    public void testDelete() {
        CloudMineURLBuilder builder = new CloudMineURLBuilder(APP_ID).deleteAll();
        String expectedUrl = expectedBaseUrl() + "/data?all=true";
        assertEquals(expectedUrl, builder.urlString());
        assertEquals(expectedUrl, builder.url().toString());
    }

    @Test
    public void testImmutable() {
        CloudMineURLBuilder builder = new CloudMineURLBuilder(APP_ID);

        CloudMineURLBuilder modifiedBuilder = builder.addQuery("all", "true");
        assertNotSame(builder, modifiedBuilder);
    }

    @Test
    public void testUser() {
        CloudMineURLBuilder builder = new CloudMineURLBuilder(APP_ID);

        assertEquals("/" + APP_ID + "/user", builder.user().appPath());
    }

    @Test
    public void testExtractAppId() {
        assertEquals("/" + APP_ID, CloudMineURLBuilder.extractAppId(expectedBaseUrl()));
        assertEquals("/" + APP_ID + "/user", CloudMineURLBuilder.extractAppId(new CloudMineURLBuilder(APP_ID).user().urlString()));
    }

    private String expectedBaseUrl() {
        return CloudMineURLBuilder.CLOUD_MINE_URL + CloudMineURLBuilder.DEFAULT_VERSION + CloudMineURLBuilder.APP + BaseURL.SEPARATOR + APP_ID;
    }

}

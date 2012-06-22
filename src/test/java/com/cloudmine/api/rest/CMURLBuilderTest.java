package com.cloudmine.api.rest;

import com.cloudmine.api.CMPagingOptions;
import com.cloudmine.api.CMRequestOptions;
import com.cloudmine.api.CMServerFunction;
import com.cloudmine.api.CMSortOptions;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

/**
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 * CMUser: johnmccarthy
 * Date: 5/16/12, 11:57 AM
 */
public class CMURLBuilderTest {

    public static final String APP_ID = "taskList";

    @Test
    public void testGet() {
        CMURLBuilder builder = new CMURLBuilder(APP_ID);
        String expectedUrl = expectedBaseUrl();
        assertEquals(expectedUrl, builder.asUrlString());

        expectedUrl += "/text";
        assertEquals(expectedUrl, builder.text().asUrlString());
    }

    @Test
    public void testSearch() {
        CMURLBuilder builder = new CMURLBuilder(APP_ID).search("[ingredients=\"chicken\"]");
        assertEquals(expectedBaseUrl() + "/search?q=%5Bingredients%3D%22chicken%22%5D", builder.asUrlString());
    }

    @Test
    public void testObjectIds() {
        CMURLBuilder builder = new CMURLBuilder(APP_ID).text().objectIds(Arrays.asList("noSpaces", "some spaces"));
        assertEquals(expectedBaseUrl() + "/text?keys=noSpaces,some+spaces", builder.asUrlString());
    }

    @Test
    public void testAccount() {
        CMURLBuilder builder = new CMURLBuilder(APP_ID).account();
        assertEquals(expectedBaseUrl() + "/account", builder.asUrlString());

        assertEquals(expectedBaseUrl() + "/account/create", builder.create().asUrlString());
    }

    @Test
    public void testDelete() {
        CMURLBuilder builder = new CMURLBuilder(APP_ID).deleteAll();
        String expectedUrl = expectedBaseUrl() + "/data?all=true";
        assertEquals(expectedUrl, builder.asUrlString());
        assertEquals(expectedUrl, builder.getUrl().toString());
    }

    @Test
    public void testImmutable() {
        CMURLBuilder builder = new CMURLBuilder(APP_ID);

        CMURLBuilder modifiedBuilder = builder.addQuery("all", "true");
        assertNotSame(builder, modifiedBuilder);
    }

    @Test
    public void testUser() {
        CMURLBuilder builder = new CMURLBuilder(APP_ID);

        assertEquals("/" + APP_ID + "/user", builder.user().getApplicationPath());
    }

    @Test
    public void testOptions() {
        CMURLBuilder builder = new CMURLBuilder(APP_ID);

        CMRequestOptions requestOptions = CMRequestOptions.CMRequestOptions(
                CMPagingOptions.CMPagingOptions(5, 10, true),
                CMServerFunction.CMServerFunction("cool Snippet", false),
                CMSortOptions.NONE);
        String expectedUrl = expectedBaseUrl() + "/text?keys=one,two,three+and+four&limit=5&skip=10&count=true&f=cool+Snippet&result_only=false&async=false";
        assertEquals(expectedUrl, builder.text().objectIds(Arrays.asList("one", "two", "three and four")).options(requestOptions).asUrlString());
    }

    @Test
    public void testExtractAppId() {
        assertEquals("/" + APP_ID, CMURLBuilder.extractAppId(expectedBaseUrl()));
        assertEquals("/" + APP_ID + "/user", CMURLBuilder.extractAppId(new CMURLBuilder(APP_ID).user().asUrlString()));
    }

    private String expectedBaseUrl() {
        return CMURLBuilder.CLOUD_MINE_URL + CMURLBuilder.DEFAULT_VERSION + CMURLBuilder.APP + BaseURL.SEPARATOR + APP_ID;
    }

}

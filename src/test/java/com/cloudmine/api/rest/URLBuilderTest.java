package com.cloudmine.api.rest;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/16/12, 11:48 AM
 */
public class URLBuilderTest {

    public static final String BASE_URL = "http://www.twitter.com";

    @Test
    public void testUrl() {
        URLBuilder builder = new URLBuilder(BASE_URL);
        String expectedUrl = BASE_URL;
        assertEquals(expectedUrl, builder.asUrlString());

        //Test that trailing "/" are removed
        assertEquals(BASE_URL, new URLBuilder(BASE_URL + "/").asUrlString());

        builder = builder.addAction("statuses")
                    .addAction("home_timeline");
        expectedUrl += "/statuses/home_timeline";
        assertEquals(expectedUrl, builder.asUrlString());

        builder = builder.addQuery("user", "john");
        expectedUrl += "?user=john";
        assertEquals(expectedUrl, builder.asUrlString());
    }
}

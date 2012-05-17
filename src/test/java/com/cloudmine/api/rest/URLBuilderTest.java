package com.cloudmine.api.rest;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/16/12, 11:48 AM
 */
public class URLBuilderTest {

    public static final String BASE_URL = "http://www.twitter.com";

    @Test
    public void testUrl() {
        URLBuilder builder = new URLBuilder(BASE_URL);
        String expectedUrl = BASE_URL;
        assertEquals(expectedUrl, builder.url());

        //Test that trailing "/" are removed
        assertEquals(BASE_URL, new URLBuilder(BASE_URL + "/").url());

        builder = builder.addAction("statuses")
                    .addAction("home_timeline");
        expectedUrl += "/statuses/home_timeline";
        assertEquals(expectedUrl, builder.url());

        builder = builder.addQuery("user", "john");
        expectedUrl += "?user=john";
        assertEquals(expectedUrl, builder.url());
    }
}

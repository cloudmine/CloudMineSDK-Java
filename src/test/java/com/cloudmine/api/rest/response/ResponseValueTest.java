package com.cloudmine.api.rest.response;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 6/7/12, 3:46 PM
 */
public class ResponseValueTest {


    @Test
    public void testResponseValue() {
        assertEquals(ResponseValue.MISSING, ResponseValue.getValue(null));
        assertEquals(ResponseValue.CREATED, ResponseValue.getValue("created"));
        assertEquals(ResponseValue.UPDATED, ResponseValue.getValue("updated"));
    }
}

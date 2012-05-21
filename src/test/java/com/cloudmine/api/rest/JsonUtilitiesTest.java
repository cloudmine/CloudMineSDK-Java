package com.cloudmine.api.rest;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/21/12, 1:43 PM
 */
public class JsonUtilitiesTest {

    @Test
    public void testQuote() {
        assertEquals("\"\"", JsonUtilities.addQuotes(null));
        assertEquals("\"bob\"", JsonUtilities.addQuotes("bob"));
    }


}

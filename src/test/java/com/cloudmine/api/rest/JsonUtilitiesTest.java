package com.cloudmine.api.rest;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

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

    @Test
    public void testIsJsonEquivalent() {
        String nicelyFormated = "{\n" +
                "\"topLevelKey\":{\n" +
                "    \"name\":\"john\",\n" +
                "    \"numbers\":[1, 2, 3, 4, 5],\n" +
                "    \"boolean\":false,\n" +
                "    \"child\":{\n" +
                "        \"friends\":[\"fred\", \"ted\", \"ben\"]\n" +
                "    }\n" +
                "}\n" +
                "}";
        String scrunched = "{\"topLevelKey\":{  \"child\":{\"friends\":[\"fred\", \"ted\", \"ben\"]},\"name\":\"john\",\"numbers\":[1, 2, 3, 4, 5],\"boolean\":false}}}";
        assertTrue(JsonUtilities.isJsonEquivalent(nicelyFormated, scrunched));
    }


}

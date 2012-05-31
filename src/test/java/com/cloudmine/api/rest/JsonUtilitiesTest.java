package com.cloudmine.api.rest;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.*;

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
    public void testDateFormatting() {
        Date date = new Date();

        Map<String, Object> objectMap = new HashMap<String, Object>();
        objectMap.put("deadline", date);
        JsonNode dateJson = JsonUtilities.mapToJsonNode(objectMap);
        String json = JsonUtilities.mapToJson(objectMap);
        Map<String, Object> convertedMap = JsonUtilities.jsonToMap(json);
        Date parsedDate = JsonUtilities.jsonClassToDate(convertedMap.get("deadline").toString());
        long dateTimeDifference = parsedDate.getTime() - date.getTime();
        assertTrue(-1000 < dateTimeDifference && dateTimeDifference < 1000);
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


        String missingKey = "{\n" +
                "\"topLevelKey\":{\n" +
                "    \"numbers\":[1, 2, 3, 4, 5],\n" +
                "    \"boolean\":false,\n" +
                "    \"child\":{\n" +
                "        \"friends\":[\"fred\", \"ted\", \"ben\"]\n" +
                "    }\n" +
                "}\n" +
                "}";
        assertFalse(JsonUtilities.isJsonEquivalent(missingKey, scrunched));

        String extraKey ="{\n" +
                "\"topLevelKey\":{\n" +
                "    \"name\":\"john\",\n" +
                "    \"extraKey\":\"this is extra\",\n" +
                "    \"numbers\":[1, 2, 3, 4, 5],\n" +
                "    \"boolean\":false,\n" +
                "    \"child\":{\n" +
                "        \"friends\":[\"fred\", \"ted\", \"ben\"]\n" +
                "    }\n" +
                "}\n" +
                "}";
        assertFalse(JsonUtilities.isJsonEquivalent(extraKey, scrunched));
    }


}

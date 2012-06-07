package com.cloudmine.api.rest;

import org.junit.Test;

import java.util.Arrays;
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

    public static final Date dateValue = new Date(439574359743594000L);
    public static final String COMPLEX_UNWRAPPED_JSON_OBJECT =
            "\"name\":\"john\",\n" +
            "\"numbers\":[1, 2, 3, 4, 5],\n" +
            "\"boolean\":true,\n" +
            "\"date\":{\n" +
            "\"__class__\":\"datetime\",\n" +
            "\"timestamp\":439574359743594\n" +
            "},\n" +
            "\"child\":{\n" +
            "\"friends\":[\"fred\", \"ted\", \"ben\"]\n" +
            "}\n";
    public static final String COMPLEX_JSON_OBJECT = "{\n" +
            COMPLEX_UNWRAPPED_JSON_OBJECT +
            "}";
    public static final String COMPLEX_UNWRAPPED_KEYED_JSON_OBJECT = "\"key\":" +
            COMPLEX_JSON_OBJECT;

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
        String json = JsonUtilities.mapToJson(objectMap);
        Map<String, Object> convertedMap = JsonUtilities.jsonToMap(json);
        Date parsedDate = (Date)convertedMap.get("deadline");
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

    @Test
    public void testDateToJsonClass() {
        Date date = new Date(3425345000L);
        String expectedJsonDate = "{\n" +
                "\"__class__\":\"datetime\",\n" +
                "\"timestamp\":3425345\n" +
                "}";
        String jsonDate = JsonUtilities.dateToJsonClass(date);
        assertEquals(expectedJsonDate, jsonDate);
    }

    @Test
    public void testJsonCollection() {
        assertTrue(JsonUtilities.isJsonEquivalent(COMPLEX_JSON_OBJECT, JsonUtilities.jsonCollection(COMPLEX_UNWRAPPED_JSON_OBJECT)));
        String jsonCollectionString = JsonUtilities.jsonCollection(COMPLEX_UNWRAPPED_KEYED_JSON_OBJECT, "\"simple\":{\"key\":100}");
        Map<String, Object> jsonMap = JsonUtilities.jsonToMap(jsonCollectionString);
        assertEquals(2, jsonMap.size());
    }

    @Test
    public void testMapToJson() {
        Map<String, Object> jsonMap = createComplexObjectMap();
        String json = JsonUtilities.mapToJson(jsonMap);

        assertTrue(JsonUtilities.isJsonEquivalent(COMPLEX_JSON_OBJECT, json));
    }

    @Test
    public void testJsonToMap() {
        Map<String, Object> jsonMap = JsonUtilities.jsonToMap(COMPLEX_JSON_OBJECT);

        assertEquals(createComplexObjectMap(), jsonMap);
    }


    public static Map<String, Object> createComplexObjectMap() {

        Map<String, Object> contentsMap = new HashMap<String, Object>();
        contentsMap.put("name", "john");
        contentsMap.put("numbers", Arrays.asList(1, 2, 3, 4, 5));
        contentsMap.put("boolean", Boolean.TRUE);
        contentsMap.put("date", dateValue);
        Map<String, Object> childObject = new HashMap<String, Object>();
        childObject.put("friends", Arrays.asList("fred", "ted", "ben"));
        contentsMap.put("child", childObject);
        return contentsMap;
    }


}

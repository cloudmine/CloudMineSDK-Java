package com.cloudmine.api.rest;

import com.cloudmine.api.CMGeoPoint;
import com.cloudmine.api.CMObject;
import com.cloudmine.api.SimpleCMObject;
import com.cloudmine.api.persistance.ClassNameRegistry;
import com.cloudmine.test.ExtendedCMObject;
import com.cloudmine.test.ExtendedCMUser;
import com.cloudmine.test.SimpleExtendedCMObject;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.*;

/**
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 * CMUser: johnmccarthy
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
        String jsonDate = JsonUtilities.convertDateToJsonClass(date);
        assertEquals(expectedJsonDate, jsonDate);
    }

    @Test
    public void testJsonCollection() {
        assertTrue(JsonUtilities.isJsonEquivalent(COMPLEX_JSON_OBJECT, JsonUtilities.jsonCollection(COMPLEX_UNWRAPPED_JSON_OBJECT).asJson()));
        String jsonCollectionString = JsonUtilities.jsonCollection(COMPLEX_UNWRAPPED_KEYED_JSON_OBJECT, "\"simple\":{\"key\":100}").asJson();
        Map<String, Object> jsonMap = JsonUtilities.jsonToMap(jsonCollectionString);
        assertEquals(2, jsonMap.size());
    }

    @Test
    public void testDateReplacement() {
        SimpleCMObject object = SimpleCMObject.SimpleCMObject("someKey", createComplexObjectMap());
        assertEquals(dateValue, object.getDate("date"));
    }

    @Test
    public void testUnwrap() {
        String simpleJson = "{\"key\":55}";
        String unwrappedSimpleJson = "\"key\":55";
        assertEquals(unwrappedSimpleJson, JsonUtilities.unwrap(simpleJson));


        String json = "\n     { \"key\":value, \"objectKey\":{ \"someObject\":55 } }";
        String unwrappedJson = "\n      \"key\":value, \"objectKey\":{ \"someObject\":55 } ";
        assertEquals(unwrappedJson, JsonUtilities.unwrap(json));
    }

    @Test
    public void testMapToJson() {
        Map<String, Object> jsonMap = createComplexObjectMap();
        String json = JsonUtilities.mapToJson(jsonMap);

        assertTrue(JsonUtilities.isJsonEquivalent(COMPLEX_JSON_OBJECT, json));

        SimpleCMObject pictureObject = SimpleCMObject.SimpleCMObject();
        pictureObject.setClass("task");
        pictureObject.add("dueDate", new Date());
        pictureObject.add("taskName", "gggg");
        pictureObject.add("isDone", true);
        pictureObject.add("priority", 0);
        pictureObject.add("location", CMGeoPoint.CMGeoPoint(50, 50));
        pictureObject.add("picture", "pictureKey");
        try {
            pictureObject.asJson(); //this is an ugly test but it routes through mapToJson and this used to fail
        }catch(Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testJsonToMap() {
        Map<String, Object> jsonMap = JsonUtilities.jsonToMap(COMPLEX_JSON_OBJECT);

        assertEquals(createComplexObjectMap(), jsonMap);
    }

    @Test
    public void testExtendedCMObjectConversionToObjectIdMappedCollection() {
        ClassNameRegistry.register("govna", ExtendedCMObject.class);
        String name = "fred";
        Date date = new Date();
        int number = 5;
        CMObject convertableObject = new ExtendedCMObject(name, date, number);

        SimpleCMObject simpleObject = SimpleCMObject.SimpleCMObject(convertableObject.getObjectId());
        simpleObject.add("name", name);
        simpleObject.add("date", date);
        simpleObject.add("number", number);
        simpleObject.add("otherExtendedObjects", new HashMap<String, ExtendedCMObject>());
        simpleObject.setClass(convertableObject.getClassName());
        String json = JsonUtilities.objectsToJson(convertableObject);
        assertTrue(JsonUtilities.isJsonEquivalent(json, simpleObject.asJson()));

        Map<String, ExtendedCMObject> map = JsonUtilities.jsonToClassMap(json, ExtendedCMObject.class);
        assertEquals(convertableObject, map.get(convertableObject.getObjectId()));

        Map<String, CMObject> objectMap = JsonUtilities.jsonToClassMap(json);
        assertEquals(convertableObject, objectMap.get(convertableObject.getObjectId()));
    }

    @Test
    public void testExtendedCMObjectConversionToJson() {
        ClassNameRegistry.register("govna", ExtendedCMObject.class);
        String name = "fred";
        Date date = new Date();
        int number = 5;
        CMObject convertableObject = new ExtendedCMObject(name, date, number);

        String dateJson = JsonUtilities.convertDateToJsonClass(date);
        String expectedJson = "\n" +
                "{\n" +
                "\"otherExtendedObjects\":{}," +
                "\"name\":\"fred\",\n" +
                "\"date\":" + dateJson + ",\n" +
                "\"number\":5,\n" +
                "\"__id__\":\"" + convertableObject.getObjectId() + "\",\n" +
                JsonUtilities.createJsonProperty(JsonUtilities.CLASS_KEY, convertableObject.getClassName()) +
                "}";
        assertTrue(JsonUtilities.isJsonEquivalent(expectedJson, JsonUtilities.objectToJson(convertableObject)));
    }

    @Test
    public void testJsonMapToKeyMap() {
        Map<String, String> expected = new HashMap<String, String>();

        assertEquals(expected, JsonUtilities.jsonMapToKeyMap(JsonUtilities.EMPTY_JSON));


        expected.put("key", COMPLEX_JSON_OBJECT);
        assertEquals(expected, JsonUtilities.jsonMapToKeyMap(
                JsonUtilities.wrap(COMPLEX_UNWRAPPED_KEYED_JSON_OBJECT)));

        String firstObjectJson = "{ \"int\":5, \"anotherObject\":{ \"bool\":false }}";
        String secondObjectJson = "{ \"with\":\"spaces\"     \n" +
                "}";
        String thirdObjectJson = "{}";
        String json = "\n" +
                "{ \"aKey\":" + firstObjectJson + ",\n" +
                "\"another\"       :  " + secondObjectJson + ",\"allScrunch\":" + thirdObjectJson + "\n" +
                "}";
        Map<String, String> conversion = JsonUtilities.jsonMapToKeyMap(json);
        assertEquals(3, conversion.size());
        assertTrue(JsonUtilities.isJsonEquivalent(firstObjectJson, conversion.get("aKey")));
        assertTrue(JsonUtilities.isJsonEquivalent(secondObjectJson, conversion.get("another")));
        assertTrue(JsonUtilities.isJsonEquivalent(thirdObjectJson, conversion.get("allScrunch")));
    }

    @Test
    public void testMergeCMObjectUpdate() {
        SimpleExtendedCMObject cmo = new SimpleExtendedCMObject(1, "face");
        String updateJson = "{\"number\":20}";
        JsonUtilities.mergeJsonUpdates(cmo, updateJson);
        assertEquals(20, cmo.getNumber());
    }

    @Test
    public void testMergeCMUserUpdate() {
        ExtendedCMUser user = new ExtendedCMUser("daemail@email.com", "pw");
        assertNotSame("here", user.getAddress());

        String profileUpdateJson = "{\"address\":\"here\"}";
        JsonUtilities.mergeJsonUpdates(user, profileUpdateJson);
        assertEquals("here", user.getAddress());
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

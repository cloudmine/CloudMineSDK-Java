package com.cloudmine.api;

import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.JsonUtilitiesTest;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 * CMUser: johnmccarthy
 * Date: 5/24/12, 3:01 PM
 */
public class SimpleCMObjectTest {
    public static final String EMPTY_ACCESS_JSON = "\"__access__\":[],";

    @Test
    public void testKeyedMapConstructor() {
        SimpleCMObject object = createComplexObject();
        String expectedJson = "{\"topLevelKey\":{\n" +
                "    \"name\":\"john\",\n" +
                "    \"__id__\":\"topLevelKey\",\n" +
                "    \"numbers\":[1, 2, 3, 4, 5],\n" +
                EMPTY_ACCESS_JSON +
                "    \"date\":{\n" +
                "    " + JsonUtilities.createJsonProperty(JsonUtilities.CLASS_KEY, JsonUtilities.DATE_CLASS) + ",\n" +
                "    " + JsonUtilities.createJsonProperty(JsonUtilities.TIME_KEY, (JsonUtilitiesTest.dateValue.getTime() / 1000)) + "\n" +
                "},\n" +
                "    \"boolean\":true,\n" +
                "    \"child\":{\n" +
                "        \"friends\":[\"fred\", \"ted\", \"ben\"]\n" +
                "    }\n" +
                "}}";
        assertTrue(JsonUtilities.isJsonEquivalent(expectedJson, object.transportableRepresentation()));
    }

    @Test
    public void testMapConstructor() {
        Map<String, Object> topLevelKeyedMap = new HashMap<String, Object>();
        Map<String, Object> unkeyedMap = new HashMap<String, Object>();
        topLevelKeyedMap.put("topLevelKey", unkeyedMap);
        unkeyedMap.put("stringKey", "aString");
        unkeyedMap.put("intKey", Integer.valueOf(5));

        SimpleCMObject topKeyed = SimpleCMObject.SimpleCMObject(topLevelKeyedMap);
        assertEquals("topLevelKey", topKeyed.getObjectId());
        assertEquals(Integer.valueOf(5), topKeyed.getInteger("intKey"));

        SimpleCMObject unkeyed = SimpleCMObject.SimpleCMObject(unkeyedMap);
        assertEquals("aString", unkeyed.getString("stringKey"));
        assertNotNull(unkeyed.getObjectId());

        Map<String, Object> unkeyedSingleEntry = new HashMap<String, Object>();
        unkeyedSingleEntry.put("stringKey", "aString");
        SimpleCMObject unkeyedSingle = SimpleCMObject.SimpleCMObject(unkeyedSingleEntry);
        assertFalse("stringKey".equals(unkeyedSingle.getObjectId()));
        assertEquals("aString", unkeyedSingle.getString("stringKey"));
    }

    @Test
    public void testKeyConstructor() {
        SimpleCMObject object = SimpleCMObject.SimpleCMObject("topLevelKey");
        object.add("string", "value");

        assertEquals("topLevelKey", object.getObjectId());
    }

    private SimpleCMObject createComplexObject() {
        return SimpleCMObject.SimpleCMObject("topLevelKey", JsonUtilitiesTest.createComplexObjectMap());
    }

    @Test
    public void testDateGet() {
        SimpleCMObject object = createComplexObject();
        assertEquals(JsonUtilitiesTest.dateValue, object.getDate("date"));
    }

    @Test
    public void testGetSimpleCMObject() {
        SimpleCMObject object = createComplexObject();
        assertNotNull(object.getSimpleCMObject("child"));
    }

    @Test
    public void testGetArray() {
        SimpleCMObject object = createComplexObject();

        List<Integer> expectedList = Arrays.asList(1, 2, 3, 4, 5);
        assertEquals(expectedList, object.<Integer>getList("numbers"));

        Map<String, Object> lotsOfListsMap = new HashMap<String, Object>();
        List<Double> doubleList = Arrays.asList(1.2, 2.3, 50000.1);
        List<String> stringList = Arrays.asList("one", "two");
        List<Boolean> booleanList = Arrays.asList(Boolean.TRUE, Boolean.FALSE);
        List<Object> nullList = Arrays.asList(null, null, null);
        lotsOfListsMap.put("double", doubleList);
        lotsOfListsMap.put("string", stringList);
        lotsOfListsMap.put("boolean", booleanList);
        lotsOfListsMap.put("null", nullList);
        SimpleCMObject listObject = SimpleCMObject.SimpleCMObject(lotsOfListsMap);

        assertEquals(doubleList, listObject.getList("double"));
        assertEquals(stringList, listObject.getList("string"));
        assertEquals(booleanList, listObject.getList("boolean"));
        assertEquals(nullList, listObject.getList("null"));
    }

    @Test
    public void testGet() {
        SimpleCMObject object = createComplexObject();

        assertEquals("john", object.get("name"));
        assertNull(object.get("friends"));
        assertFalse(object.getBoolean("non existent boolean", Boolean.FALSE));
        assertTrue(object.getBoolean("boolean"));

        CMGeoPoint geoPoint = CMGeoPoint.CMGeoPoint(3.3, 4);
        object.add("location", geoPoint);
        assertEquals(geoPoint, object.getGeoPoint("location"));

    }

    @Test
    public void testGeoAsJson() {
        SimpleCMObject object = SimpleCMObject.SimpleCMObject("topLevelKey");
        CMGeoPoint geoObject = CMGeoPoint.CMGeoPoint(50, 50, "geoObject");
        object.add("geo", geoObject);
        String expectedJson = "{\"topLevelKey\":{\"__id__\":\"topLevelKey\", " + EMPTY_ACCESS_JSON +
                "\"geo\":{\"__id__\":\"geoObject\"," + EMPTY_ACCESS_JSON + " \"__type__\":\"geopoint\",\"longitude\":50.0,\"latitude\":50.0,\"__class__\":\"CMGeoPoint\"}}}";

        assertTrue("Expected: \n" + expectedJson + "\nbut got: \n" + object.transportableRepresentation(), JsonUtilities.isJsonEquivalent(expectedJson, object.transportableRepresentation()));


        //difference is in the key value - geo vs geoObject
        object = SimpleCMObject.SimpleCMObject("topLevelKey");
        object.add(geoObject);
        expectedJson = "{\"topLevelKey\":{\"__id__\":\"topLevelKey\"," + EMPTY_ACCESS_JSON + "\"geoObject\":{\"__id__\":\"geoObject\", \"__type__\":\"geopoint\", " + EMPTY_ACCESS_JSON + "\"longitude\":50.0,\"latitude\":50.0,\"__class__\":\"CMGeoPoint\"}}}";

        assertTrue("Expected: \n" + expectedJson + "\nbut got: \n" + object.transportableRepresentation(), JsonUtilities.isJsonEquivalent(expectedJson, object.transportableRepresentation()));

    }
}

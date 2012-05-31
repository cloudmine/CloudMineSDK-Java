package com.cloudmine.api;

import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.JsonUtilitiesTest;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/24/12, 3:01 PM
 */
public class SimpleCMObjectTest {

    @Test
    public void testConstructor() {
        SimpleCMObject object = createComplexObject();
        String expectedJson = "{\"topLevelKey\":{\n" +
                "    \"name\":\"john\",\n" +
                "    \"numbers\":[1, 2, 3, 4, 5],\n" +
                "    \"date\":{\n" +
                "    " + JsonUtilities.createJsonProperty(JsonUtilities.CLASS_KEY, JsonUtilities.DATE_CLASS) + ",\n" +
                "    " + JsonUtilities.createJsonProperty(JsonUtilities.TIME_KEY, (JsonUtilitiesTest.dateValue.getTime() / 1000)) + "\n" +
                "},\n" +
                "    \"boolean\":true,\n" +
                "    \"child\":{\n" +
                "        \"friends\":[\"fred\", \"ted\", \"ben\"]\n" +
                "    }\n" +
                "}}";
        assertTrue(JsonUtilities.isJsonEquivalent(expectedJson, object.asJson()));
    }

    private SimpleCMObject createComplexObject() {
        return new SimpleCMObject("topLevelKey", JsonUtilitiesTest.createComplexObjectMap());
    }

    @Test
    public void testDateGet() {

    }

    @Test
    public void testGet() {
        SimpleCMObject object = createComplexObject();

        assertEquals("john", object.get("name"));
        assertNull(object.get("friends"));
        assertFalse(object.getBoolean("non existent boolean", Boolean.FALSE));
        assertTrue(object.getBoolean("boolean"));

    }
}

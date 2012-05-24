package com.cloudmine.api;

import com.cloudmine.api.rest.JsonUtilities;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
        String expectedJson = "\"topLevelKey\":{\n" +
                "    \"name\":\"john\",\n" +
                "    \"numbers\":[1, 2, 3, 4, 5],\n" +
                "    \"boolean\":true,\n" +
                "    \"child\":{\n" +
                "        \"friends\":{\"fred\", \"ted\", \"ben\"}\n" +
                "    }\n" +
                "}";
        assertTrue(JsonUtilities.isJsonEquivalent(expectedJson, object.asKeyedObject()));
    }

    private SimpleCMObject createComplexObject() {
        Map<String, Object> contentsMap = new HashMap<String, Object>();
        contentsMap.put("name", "john");
        contentsMap.put("numbers", Arrays.asList(1, 2, 3, 4, 5));
        contentsMap.put("boolean", Boolean.TRUE);
        Map<String, Object> childObject = new HashMap<String, Object>();
        childObject.put("friends", Arrays.asList("fred", "ted", "ben"));
        contentsMap.put("child", childObject);
        return new SimpleCMObject("topLevelKey", contentsMap);
    }

    @Test
    public void testGet() {
        SimpleCMObject object = createComplexObject();

        assertEquals("john", object.get("name"));
        assertNull(object.get("friends"));
        assertFalse(object.getBoolean("non existent boolean"));
        assertTrue(object.getBoolean("boolean"));

    }
}

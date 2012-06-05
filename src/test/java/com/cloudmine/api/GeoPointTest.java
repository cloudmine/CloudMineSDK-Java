package com.cloudmine.api;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 6/4/12, 12:08 PM
 */
public class GeoPointTest {

    @Test
    public void testConstructor() {
        GeoPoint point = new GeoPoint(23.5, 100.1);

        assertEquals(100.1, point.latitude());

        point = new GeoPoint("{\"location\": {\n" +
                "            \"__type__\": \"geopoint\",\n" +
                "            \"x\": 45.5,\n" +
                "            \"lat\": -70.2\n" +
                "        }}");
        assertEquals(-70.2, point.latitude());
        assertEquals(45.5, point.longitude());

        GeoPoint duplicatePoint = new GeoPoint(point.asJson());
        assertEquals(point, duplicatePoint);
    }
}

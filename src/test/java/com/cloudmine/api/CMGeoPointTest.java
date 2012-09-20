package com.cloudmine.api;

import com.cloudmine.api.persistance.ClassNameRegistry;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.TransportableString;
import com.cloudmine.test.GeoCMObject;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 * CMUser: johnmccarthy
 * Date: 6/4/12, 12:08 PM
 */
public class CMGeoPointTest {

    @Test
    public void testConstructor() {
        CMGeoPoint point = new CMGeoPoint(23.5, 100.1);

        assertEquals(100.1, point.getLatitude());

        point = new CMGeoPoint(new TransportableString("{\"location\": {\n" +
                "            \"__type__\": \"geopoint\",\n" +
                "            \"x\": 45.5,\n" +
                "            \"lat\": -70.2\n" +
                "        }}"));
        assertEquals(-70.2, point.getLatitude());
        assertEquals(45.5, point.getLongitude());

        CMGeoPoint duplicatePoint = new CMGeoPoint(45.5, -70.2);
        assertEquals(point, duplicatePoint);
    }

    @Test
    public void testSubObject() {
        ClassNameRegistry.register("CMGeoPoint", CMGeoPoint.class);
        GeoCMObject object = JsonUtilities.jsonToClass("{\"geoPoint\":{\"__type__\":\"geopoint\",\"longitude\":55.0,\"latitude\":55.0,\"__class__\":\"CMGeoPoint\"},\"__id__\":\"4bac92ba-6f40-4b9f-8785-c0cf1adc152e\",\"__access__\":[],\"__class__\":\"com.cloudmine.test.GeoCMObject\"}", GeoCMObject.class);
        assertNotNull(object.getGeoPoint());
        assertEquals(55.0, object.getGeoPoint().getLatitude());
    }
}

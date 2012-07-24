package com.cloudmine.api;

import com.cloudmine.api.rest.TransportableString;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 * CMUser: johnmccarthy
 * Date: 6/4/12, 12:08 PM
 */
public class CMGeoPointTest {

    @Test
    public void testConstructor() {
        CMGeoPoint point = CMGeoPoint.CMGeoPoint(23.5, 100.1);

        assertEquals(100.1, point.getLatitude());

        point = CMGeoPoint.CMGeoPoint(new TransportableString("{\"location\": {\n" +
                "            \"__type__\": \"geopoint\",\n" +
                "            \"x\": 45.5,\n" +
                "            \"lat\": -70.2\n" +
                "        }}"));
        assertEquals(-70.2, point.getLatitude());
        assertEquals(45.5, point.getLongitude());

        CMGeoPoint duplicatePoint = CMGeoPoint.CMGeoPoint(point);
        assertEquals(point, duplicatePoint);
    }
}

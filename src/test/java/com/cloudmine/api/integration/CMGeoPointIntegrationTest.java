package com.cloudmine.api.integration;

import com.cloudmine.api.CMGeoPoint;
import com.cloudmine.api.CMType;
import com.cloudmine.api.SimpleCMObject;
import com.cloudmine.api.rest.CMStore;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.callbacks.CMObjectResponseCallback;
import com.cloudmine.api.rest.response.CMObjectResponse;
import com.cloudmine.test.GeoCMObject;
import com.cloudmine.test.ServiceTestBase;
import org.junit.Test;

import static com.cloudmine.test.AsyncTestResultsCoordinator.waitThenAssertTestResults;
import static com.cloudmine.test.TestServiceCallback.testCallback;
import static org.junit.Assert.assertEquals;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMGeoPointIntegrationTest extends ServiceTestBase {



    @Test
    public void testGeoPoint() {
        final GeoCMObject geoObject = new GeoCMObject();
        final CMGeoPoint geoPoint = new CMGeoPoint(55, 55);

        geoObject.setGeoPoint(geoPoint);

        geoObject.save(hasSuccess);
        waitThenAssertTestResults();

        CMStore.getStore().loadApplicationObjectWithObjectId(geoObject.getObjectId(), testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                GeoCMObject loadedGeoObject = (GeoCMObject)response.getCMObject(geoObject.getObjectId());
                assertEquals(geoPoint, loadedGeoObject.getGeoPoint());
            }

        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testOtherKeyedGeoPoint() {
        final SimpleCMObject otherGeo = new SimpleCMObject();
        otherGeo.add(JsonUtilities.TYPE_KEY, CMType.GEO_POINT.getTypeId());
        otherGeo.add("x", 30);
        otherGeo.add("lat", 40);
        otherGeo.save(hasSuccess);
        waitThenAssertTestResults();

        service.asyncLoadObject(otherGeo.getObjectId(), testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                CMGeoPoint point = response.getCMObject(otherGeo.getObjectId(), CMGeoPoint.class);
                assertEquals(30.0, point.getLongitude(), .1);
                assertEquals(40, point.getLatitude(), .1);
            }
        }));
        waitThenAssertTestResults();
    }
}

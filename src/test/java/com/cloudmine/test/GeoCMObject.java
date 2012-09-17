package com.cloudmine.test;

import com.cloudmine.api.CMGeoPoint2;
import com.cloudmine.api.CMObject;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class GeoCMObject extends CMObject {

    private CMGeoPoint2 geoPoint;

    public GeoCMObject() {

    }

    public GeoCMObject(CMGeoPoint2 point) {
        this.geoPoint = point;
    }

    public CMGeoPoint2 getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(CMGeoPoint2 geoPoint) {
        this.geoPoint = geoPoint;
    }
}

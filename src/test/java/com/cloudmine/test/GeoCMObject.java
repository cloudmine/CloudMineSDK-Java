package com.cloudmine.test;

import com.cloudmine.api.CMGeoPoint;
import com.cloudmine.api.CMObject;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class GeoCMObject extends CMObject {

    private CMGeoPoint geoPoint;

    public GeoCMObject() {

    }

    public GeoCMObject(CMGeoPoint point) {
        this.geoPoint = point;
    }

    public CMGeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(CMGeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }
}

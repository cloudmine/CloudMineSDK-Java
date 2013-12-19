package com.cloudmine.test;

import com.cloudmine.api.CMGeoPoint;
import com.cloudmine.api.CMGeoPointInterface;
import com.cloudmine.api.CMObject;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class GeoCMObject extends CMObject {

    @JsonDeserialize(as = CMGeoPoint.class)
    private CMGeoPointInterface geoPoint;

    public GeoCMObject() {

    }

    public GeoCMObject(CMGeoPointInterface point) {
        this.geoPoint = point;
    }

    public CMGeoPointInterface getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(CMGeoPointInterface geoPoint) {
        this.geoPoint = geoPoint;
    }
}

package com.cloudmine.api;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMGeoPoint2 extends CMObject{

    private double latitude;
    private double longitude;
    private final String __type__ = "geopoint";

    public CMGeoPoint2() {

    }

    public CMGeoPoint2(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String getClassName() {
        return CMGeoPoint.GEOPOINT_CLASS;
    }

    public String get__type__() {
        return __type__;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}

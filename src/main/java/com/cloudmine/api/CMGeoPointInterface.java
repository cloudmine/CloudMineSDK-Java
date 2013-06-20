package com.cloudmine.api;

import com.cloudmine.api.rest.Savable;
import com.cloudmine.api.rest.Transportable;
import com.cloudmine.api.rest.response.ObjectModificationResponse;

/**
 * Stored on the server as a location, which can be searched for based on distance from another point. A GeoPoint consists
 * of a latitude and longitude
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public interface CMGeoPointInterface extends Transportable, Savable<ObjectModificationResponse, ObjectModificationResponse> {
    static final String GEOPOINT_CLASS = "CMGeoPoint";
    static final String LONGITUDE_KEY = "longitude";
    static final String LATITUDE_KEY = "latitude";

    String getClassName();

    double getLatitude();

    void setLatitude(double latitude);

    double getLongitude();

    void setLongitude(double longitude);
}

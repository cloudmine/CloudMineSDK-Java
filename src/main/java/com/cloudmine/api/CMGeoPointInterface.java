package com.cloudmine.api;

import com.cloudmine.api.rest.Savable;
import com.cloudmine.api.rest.Transportable;
import com.cloudmine.api.rest.response.ObjectModificationResponse;

/**
 *
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

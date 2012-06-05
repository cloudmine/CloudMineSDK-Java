package com.cloudmine.api;

import com.cloudmine.api.exceptions.JsonConversionException;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 6/4/12, 11:57 AM
 */
public class GeoPoint extends SimpleCMObject {
    private static final String[] LATITUDE_KEYS = {"latitude", "lat", "y"};
    private static final String[] LONGITUDE_KEYS = {"longitude", "lon", "x"};
    public static final String GEOPOINT_TYPE = "geopoint";
    public static final String GEOPOINT_CLASS = "CMGeoPoint";
    public static final String LONGITUDE_KEY = "longitude";
    public static final String LATITUDE_KEY = "latitude";

    public GeoPoint(double longitude, double latitude) {
        super();
        setClass(GEOPOINT_CLASS);
        setType(CloudMineType.GEO_POINT);
        add(LONGITUDE_KEY, longitude);
        add(LATITUDE_KEY, latitude);
    }

    /**
     * Constructs a geopoint from the given Json. Is assumed to be in the format { "key": {geopoint json}}
     * @param json
     * @throws JsonConversionException
     */
    public GeoPoint(String json) throws JsonConversionException {
        super(json);
        boolean isMissingAnything = !(isType(CloudMineType.GEO_POINT) && hasLatitude() && hasLongitude());
        if(isMissingAnything) {
            throw new JsonConversionException("Given non geopoint class to construct geopoint: " + json);
        }
        setClass(GEOPOINT_CLASS);
        setType(CloudMineType.GEO_POINT);
    }

    private boolean hasLatitude() {
        return hasKeyNumber(LATITUDE_KEYS);
    }

    private boolean hasLongitude() {
        return hasKeyNumber(LONGITUDE_KEYS);
    }

    private boolean hasKeyNumber(String[] keys) {
        for(int i = 0; i < keys.length; i++) {
            Object toGet = get(keys[i]);
            if(toGet != null && toGet instanceof Number) {
                return true;
            }
        }
        return false;
    }

    public double longitude() {
        return getDouble(LONGITUDE_KEYS);
    }

    public double latitude() {
        return getDouble(LATITUDE_KEYS);
    }

    public String locationString() {
        return longitude() + ", " + latitude();
    }
}

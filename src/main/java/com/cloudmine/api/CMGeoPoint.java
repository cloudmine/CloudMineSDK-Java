package com.cloudmine.api;

import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.exceptions.JsonConversionException;
import com.cloudmine.api.rest.Json;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 6/4/12, 11:57 AM
 */
public class CMGeoPoint extends SimpleCMObject {
    private static final String[] LATITUDE_KEYS = {"latitude", "lat", "y"};
    private static final String[] LONGITUDE_KEYS = {"longitude", "lon", "x"};
    public static final String GEOPOINT_TYPE = "geopoint";
    public static final String GEOPOINT_CLASS = "CMGeoPoint";
    public static final String LONGITUDE_KEY = "longitude";
    public static final String LATITUDE_KEY = "latitude";

    public static CMGeoPoint CMGeoPoint(double longitude, double latitude) throws CreationException {
        return new CMGeoPoint(longitude, latitude);
    }

    public static CMGeoPoint CMGeoPoint(double longitude, double latitude, String key) throws CreationException {
        return new CMGeoPoint(longitude, latitude, key);
    }

    public static CMGeoPoint CMGeoPoint(Json json) throws CreationException {
        try {
            return new CMGeoPoint(json);
        } catch (JsonConversionException e) {
            throw new CreationException(e);
        }
    }

    CMGeoPoint(double longitude, double latitude) throws CreationException {
        this(longitude, latitude, generateUniqueKey());
    }

    CMGeoPoint(double longitude, double latitude, String key) throws CreationException {
        super(key);
        setClass(GEOPOINT_CLASS);
        setType(CMType.GEO_POINT);
        add(LONGITUDE_KEY, longitude);
        add(LATITUDE_KEY, latitude);
    }

    /**
     * Constructs a geopoint from the given Json. Is assumed to be in the format { "key": {geopoint json}}
     * @param json
     * @throws JsonConversionException
     */
    CMGeoPoint(Json json) throws JsonConversionException, CreationException {
        super(json);
        boolean isMissingAnything = !(isType(CMType.GEO_POINT) && hasLatitude() && hasLongitude());
        if(isMissingAnything) {
            throw new JsonConversionException("Given non geopoint class to construct geopoint: " + json);
        }
        setClass(GEOPOINT_CLASS);
        setType(CMType.GEO_POINT);
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

    public double longitude() throws JsonConversionException {
        return getDouble(LONGITUDE_KEYS);
    }

    public double latitude() throws JsonConversionException {
        return getDouble(LATITUDE_KEYS);
    }

    public String locationString() throws JsonConversionException {
        return longitude() + ", " + latitude();
    }
}

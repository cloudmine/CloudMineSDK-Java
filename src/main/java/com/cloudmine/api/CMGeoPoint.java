package com.cloudmine.api;

import com.cloudmine.api.exceptions.ConversionException;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.Transportable;

/**
 * A CMObject that has location information associated with it. This allows you to perform
 * geographic searches for this data. For more information, including search information, view the
 * <a href="https://cloudmine.me/docs/object-storage#query_geof">CloudMine geo query documentation</a>
 * <br>Copyright CloudMine LLC. All rights reserved<br>
 *     See LICENSE file included with SDK for details.
 */
public class CMGeoPoint extends SimpleCMObject {
    private static final String[] LATITUDE_KEYS = {"latitude", "lat", "y"};
    private static final String[] LONGITUDE_KEYS = {"longitude", "lon", "x"};
    public static final String GEOPOINT_TYPE = "geopoint";
    public static final String GEOPOINT_CLASS = "CMGeoPoint";
    public static final String LONGITUDE_KEY = "longitude";
    public static final String LATITUDE_KEY = "latitude";
    public static final String CLASS_NAME = "geopoint";


    /**
     * Instantiate a CMGeoPoint with the given latitude and longitude and a random unique objectId
     * @param longitude a double between [-180, 180)
     * @param latitude a double between [-90, 90]
     * @return a new CMGeoPoint
     */
    public CMGeoPoint(double longitude, double latitude) throws CreationException {
        super(false);
        setClass(GEOPOINT_CLASS);
        setType(CMType.GEO_POINT);
        add(LONGITUDE_KEY, longitude);
        add(LATITUDE_KEY, latitude);
    }

    /**
     * Instantiate a new CMGeoPoint based on the given transportable. The transportable must include a top level key, a "__type__":"geopoint" property,
     * and a latitude and longitude.
     * @param transportable a valid transportable representation of a CMGeoPoint
     * @throws CreationException if given improperly formated transportable; either malformed or lacking a __type__, latitude, or longitude.
     */
    public CMGeoPoint(Transportable transportable) throws ConversionException, CreationException {
        super(transportable, false);
        boolean isMissingAnything = !(isType(CMType.GEO_POINT) && hasLatitude() && hasLongitude());
        if(isMissingAnything) {
            throw new ConversionException("Given non geopoint class to construct geopoint: " + transportable);
        }
        setClass(GEOPOINT_CLASS);
        setType(CMType.GEO_POINT);
    }

    public CMGeoPoint() {
        super(false);
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

    /**
     * Get the longitude
     * @return the longitude
     * @throws ConversionException if there is a longitude value, but it is not a double
     */
    public double getLongitude() throws ConversionException {
        return getDouble(LONGITUDE_KEYS);
    }

    /**
     * Get the latitude
     * @return the latitude
     * @throws ConversionException if there is a latitude value, but it is not a double
     */
    public double getLatitude() throws ConversionException {
        return getDouble(LATITUDE_KEYS);
    }

    /**
     * A string representation of this GeoPoint
     * @return longitude, latitude
     * @throws ConversionException if a latitude/longitude value exists, but are not doubles
     */
    public String getLocationString() throws ConversionException {
        return getLongitude() + ", " + getLatitude();
    }

    @Override
    public String getClassName() {
        if(getClass() == CMGeoPoint.class) //this way if someone extends this, it will not say this is a CMGeoPoint, but whatever their subclass is
            return CLASS_NAME;
        return super.getClassName();
    }



}

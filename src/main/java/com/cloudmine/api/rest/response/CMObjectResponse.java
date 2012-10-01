package com.cloudmine.api.rest.response;

import com.cloudmine.api.CMObject;
import com.cloudmine.api.Distance;
import com.cloudmine.api.DistanceUnits;
import com.cloudmine.api.exceptions.ConversionException;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.response.code.ObjectLoadCode;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *  Returned by the CloudMine service in response to object fetch requests. Provides access to the
 *  {@link CMObject}s returned by the request
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class CMObjectResponse extends SuccessErrorResponse<ObjectLoadCode> {
    private static final Logger LOG = LoggerFactory.getLogger(CMObjectResponse.class);
    public static final String COUNT_KEY = "count";
    public static final int NO_COUNT = -1;
    public static ResponseConstructor<CMObjectResponse> CONSTRUCTOR = new ResponseConstructor<CMObjectResponse>() {
        @Override
        public CMObjectResponse construct(HttpResponse response) throws CreationException {
            return new CMObjectResponse(response);
        }
    };
    private final Map<String, ? extends CMObject> objectMap;


    /**
     * Instantiate a new CMObjectResponse. You probably should not be calling this yourself.
     * @param response a response to an object fetch request
     */
    public CMObjectResponse(HttpResponse response) {
        super(response);
        if(hasSuccess()) {
            Map<String, String> messageMap = JsonUtilities.jsonMapToKeyMap(getMessageBody());
            String success = messageMap.get(SUCCESS);
            Map<String, ? extends CMObject> tempMap;
            try {
                tempMap = JsonUtilities.jsonToClassMap(success);
            }catch(ConversionException jce) {
                tempMap = Collections.emptyMap();
                LOG.error("Trouble converting: " + success + ", using empty map");
            }
            objectMap = tempMap;
        } else {
            objectMap = Collections.emptyMap();
        }
    }

    /**
     * Internal use only
     * @param response
     * @param code
     */
    public CMObjectResponse(String response, int code) {
        super(response, code); //TODO this is copy pasta code from above :( thats bad
        if(hasSuccess()) {
            String success = JsonUtilities.jsonMapToKeyMap(getMessageBody()).get(SUCCESS);
            Map<String, ? extends CMObject> tempMap;
            try {
                tempMap = JsonUtilities.jsonToClassMap(success);
            }catch(ConversionException jce) {
                tempMap = Collections.emptyMap();
                LOG.error("Trouble converting: " + success + ", using empty map");
            }
            objectMap = tempMap;
        } else {
            objectMap = Collections.emptyMap();
        }
    }

    @Override
    public ObjectLoadCode getResponseCode() {
        return ObjectLoadCode.codeForStatus(getStatusCode());
    }

    /**
     * Get the distance measurement for a specific objectId. In order for a value to be returned, the
     * object with the given objectId must have been loaded, a geo query must have been performed, and
     * a {@link com.cloudmine.api.rest.options.CMRequestOptions} with a {@link com.cloudmine.api.rest.options.CMSearchOptions}
     * must have been specified. If this isn't the case, null will be returned
     * @param objectId
     * @return null if there isn't a distance value for the given objectId, or the Distance from the geo query point
     */
    public Distance getDistanceFor(String objectId) {
        Map<String, Object> metaMap = (Map<String, Object>) getObject("meta"); //got maps on maps on maps
        if(metaMap != null) {
            Map<String, Object> objectMap = (Map<String, Object>) metaMap.get(objectId);
            if(objectMap != null) {
                Map<String, Object> geoMap = (Map<String, Object>) objectMap.get("geo");
                if(geoMap != null) {
                    Double distance = (Double) geoMap.get("distance");
                    String unitsString = (String) geoMap.get("units");
                    DistanceUnits units = DistanceUnits.valueOf(unitsString);
                    if(distance != null && units != null) {
                        return new Distance(distance, units);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns a List of all the CMObjects fetched by the request
     * @return a List of all the CMObjects fetched by the request
     */
    public List<CMObject> getObjects() {
        return new ArrayList<CMObject>(objectMap.values());
    }

    /**
     * Get all of the objects of the specified klass
     * @param klass
     * @param <CMO>
     * @return
     */
    public <CMO extends CMObject> List<CMO> getObjects(Class<CMO> klass) {
        List<CMO> toReturn = new ArrayList<CMO>();
        for(CMObject object : getObjects()) {
            if(isReturnableFor(klass, object)) {
                toReturn.add((CMO)object);
            }
        }
        return toReturn;
    }

    private <CMO extends CMObject> boolean isReturnableFor(Class<CMO> klass, CMObject object) {
        return object != null && klass != null &&
                klass.isAssignableFrom(object.getClass());
    }

    /**
     * Returns the object with the given objectId, or null if it doesn't exist
     * @param objectId the objectId for the object
     * @return the object, or null if it was not retrieved
     */
    public CMObject getCMObject(String objectId) {
        return objectMap.get(objectId);
    }

    /**
     * Get the object with the given objectId as the specific class, or null if it doesn't exist or isn't of
     * the specified class
     * @param objectId the objectId for the object to retrieve
     * @param objectClass the class to cast the retrieved object as, if possible
     * @param <CMO>
     * @return the object, or null if it wasn't loaded in this response, or if its
     */
    public <CMO extends CMObject> CMO getCMObject(String objectId, Class<CMO> objectClass) {
        CMObject object = getCMObject(objectId);
        if(isReturnableFor(objectClass, object))
            return (CMO) object;
        return null;
    }

    /**
     * If this load was made with count=true (specified by using {@link com.cloudmine.api.rest.options.CMPagingOptions})
     * then this will return the number of entries for the query that was made, regardless of how many results
     * were returned.
     * @return the number of entries for the query that was made, or {@link #NO_COUNT} if count=true wasn't requested, or if unable to parse the count property as an Integer
     */
    public int getCount() {
        Object countObject = getObject(COUNT_KEY);
        if(countObject != null && countObject instanceof Integer) {
            return ((Integer)countObject).intValue();
        }
        return NO_COUNT;
    }
}

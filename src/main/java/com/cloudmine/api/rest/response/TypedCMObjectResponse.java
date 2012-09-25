package com.cloudmine.api.rest.response;

import com.cloudmine.api.CMObject;
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
public class TypedCMObjectResponse<CMO extends CMObject> extends SuccessErrorResponse<ObjectLoadCode> {
    private static final Logger LOG = LoggerFactory.getLogger(TypedCMObjectResponse.class);
    public static final String COUNT_KEY = "count";
    public static final int NO_COUNT = -1;

    private final Map<String, CMO> objectMap;

    public static <CMO extends CMObject> ResponseConstructor<TypedCMObjectResponse<CMO>> constructor(final Class<CMO> klass) {
        return new ResponseConstructor<TypedCMObjectResponse<CMO>>() {
            @Override
            public TypedCMObjectResponse<CMO> construct(HttpResponse response) throws CreationException {
                return new TypedCMObjectResponse<CMO>(response, klass);
            }
        };
    }

    /**
     * Instantiate a new CMObjectResponse. You probably should not be calling this yourself.
     * @param response a response to an object fetch request
     */
    public TypedCMObjectResponse(HttpResponse response, Class<CMO> klass) {
        super(response);
        if(hasSuccess()) {
            String success = JsonUtilities.jsonMapToKeyMap(getMessageBody()).get(SUCCESS);
            Map<String, CMO> tempMap;
            try {
//                if(klass == null || CMObject.class.equals(klass))
//                    tempMap = JsonUtilities.jsonToClassMap(success);
//                else
                    tempMap = JsonUtilities.<CMO>jsonToCMObjectMap(success, klass);
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
    public TypedCMObjectResponse(String response, int code, Class<CMO> klass) {
        super(response, code); //TODO this is copy pasta code from above :( thats bad

        if(hasSuccess()) {
            String success = JsonUtilities.jsonMapToKeyMap(getMessageBody()).get(SUCCESS);
            Map<String, CMO> tempMap;
            try {
//                if(klass == null || CMObject.class.equals(klass))
//                    tempMap = JsonUtilities.jsonToClassMap(success);
//                else
                tempMap = JsonUtilities.<CMO>jsonToCMObjectMap(success, klass);
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
     * Returns a List of all the CMObjects fetched by the request
     * @return a List of all the CMObjects fetched by the request
     */
    public List<CMO> getObjects() {
        return new ArrayList<CMO>(objectMap.values());
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

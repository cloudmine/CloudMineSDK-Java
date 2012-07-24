package com.cloudmine.api.rest.response;

import com.cloudmine.api.CMObject;
import com.cloudmine.api.exceptions.ConversionException;
import com.cloudmine.api.exceptions.JsonConversionException;
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
    public static final ResponseConstructor<CMObjectResponse> CONSTRUCTOR =
            new ResponseConstructor<CMObjectResponse>() {

                @Override
                public CMObjectResponse construct(HttpResponse response) {
                    return new CMObjectResponse(response);
                }
            };
    public static final String COUNT_KEY = "count";
    public static final int NO_COUNT = -1;

    private final Map<String, ? extends CMObject> objectMap;

    /**
     * Instantiate a new CMObjectResponse. You probably should not be calling this yourself.
     * @param response a response to an object fetch request
     */
    public CMObjectResponse(HttpResponse response) {
        super(response);
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

    protected CMObjectResponse(String response, int code) {
        super(response, code); //TODO this is copy pasta code from above :( thats bad
        if(hasSuccess()) {
            String success = JsonUtilities.jsonMapToKeyMap(getMessageBody()).get(SUCCESS);
            Map<String, ? extends CMObject> tempMap;
            try {
                tempMap = JsonUtilities.jsonToClassMap(success);
            }catch(JsonConversionException jce) {
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
    public List<CMObject> getObjects() {
        return new ArrayList<CMObject>(objectMap.values());
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

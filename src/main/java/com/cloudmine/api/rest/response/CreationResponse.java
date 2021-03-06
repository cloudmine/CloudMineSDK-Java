package com.cloudmine.api.rest.response;

import com.cloudmine.api.CMType;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.response.code.CMResponseCode;
import org.apache.http.HttpResponse;

/**
 * Returned by requests that create an object id for insertions, such as {@link com.cloudmine.api.JavaCMUser#createUser(com.cloudmine.api.rest.callbacks.Callback)}
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CreationResponse extends ResponseBase<CMResponseCode> {
    public static ResponseConstructor<CreationResponse> CONSTRUCTOR = new ResponseConstructor<CreationResponse>() {
        @Override
        public CreationResponse construct(HttpResponse response) throws CreationException {
            return new CreationResponse(response);
        }

        @Override
        public CreationResponse construct(String messageBody, int responseCode) throws CreationException {
            return new CreationResponse(messageBody, responseCode);
        }
    };

    protected CreationResponse(HttpResponse response) {
        super(response);
    }

    /**
     * Internal use only
     * @param body
     * @param code
     */
    public CreationResponse(String body, int code) {
        super(body, code);
    }

    @Override
    public CMResponseCode getResponseCode() {
        return CMResponseCode.codeForStatus(getStatusCode());
    }

    /**
     * Get the objectId returned by this create response
     * @return
     */
    public String getObjectId() {
        Object objectId = getObject(JsonUtilities.OBJECT_ID_KEY);
        return objectId == null ? null : objectId.toString();
    }

    public CMType getType() {
        Object type = getObject(JsonUtilities.TYPE_KEY);
        return type == null ? CMType.NONE : CMType.getTypeById(type.toString());
    }
}

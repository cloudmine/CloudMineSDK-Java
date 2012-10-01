package com.cloudmine.api.rest.response;

import com.cloudmine.api.Immutable;
import com.cloudmine.api.SimpleCMObject;
import com.cloudmine.api.exceptions.ConversionException;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.response.code.CMSocialCode;
import org.apache.http.HttpResponse;

import java.util.List;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMSocialResponse extends ResponseBase<CMSocialCode> {
    public static final ResponseConstructor<CMSocialResponse> CONSTRUCTOR = new ResponseConstructor<CMSocialResponse>() {
        @Override
        public CMSocialResponse construct(HttpResponse response) throws CreationException {
            return new CMSocialResponse(response);
        }
    };

    private final Immutable<List<SimpleCMObject>> responseAsSimpleCMObjects = new Immutable<List<SimpleCMObject>>();

    public CMSocialResponse(HttpResponse response) {
        super(response);
    }

    @Override
    public CMSocialCode getResponseCode() {
        return CMSocialCode.codeForStatus(getStatusCode());
    }

    public List<SimpleCMObject> getAsSimpleCMObjects() throws ConversionException{
        if(responseAsSimpleCMObjects.isSet())
            return responseAsSimpleCMObjects.value();
        responseAsSimpleCMObjects.setValue(JsonUtilities.socialJsonToSimpleCMObject(getMessageBody()));
        return responseAsSimpleCMObjects.value();
    }
}

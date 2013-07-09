package com.cloudmine.api.rest.response;

import com.cloudmine.api.Strings;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.response.code.CMResponseCode;
import org.apache.http.HttpResponse;

import java.util.Collections;
import java.util.List;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class PushChannelResponse extends ResponseBase<CMResponseCode> {

    public static ResponseConstructor<PushChannelResponse> CONSTRUCTOR = new ResponseConstructor<PushChannelResponse>() {
        @Override
        public PushChannelResponse construct(HttpResponse response) throws CreationException {
            return new PushChannelResponse(response);
        }
    };

    protected PushChannelResponse(HttpResponse response) {
        super(response, true);
    }

    public String getChannelName() {
        return Strings.asString(getObject("name"));
    }

    public List<String> getUserIds() {
        return getNeverNullList(getObject("user_ids"));
    }

    public List<String> getDeviceIds() {
        return getNeverNullList(getObject("device_ids"));
    }

    private List<String> getNeverNullList(Object potentialList) {
        if(potentialList instanceof List) return (List<String>) potentialList;
        else return Collections.EMPTY_LIST;
    }

    @Override
    public CMResponseCode getResponseCode() {
        return CMResponseCode.codeForStatus(getStatusCode());
    }
}

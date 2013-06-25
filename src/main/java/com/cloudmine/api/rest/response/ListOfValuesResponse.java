package com.cloudmine.api.rest.response;

import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.response.code.CMResponseCode;
import org.apache.http.HttpResponse;

import java.util.List;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class ListOfValuesResponse<VALUE> extends ResponseBase<CMResponseCode> {

    public static final <VALUE> ResponseConstructor<ListOfValuesResponse<VALUE>> CONSTRUCTOR() {
        return new ResponseConstructor<ListOfValuesResponse<VALUE>>() {
            @Override
            public ListOfValuesResponse construct(HttpResponse response) throws CreationException {
                return new ListOfValuesResponse(response);
            }
        };
    }

    private List<VALUE> values;

    public ListOfValuesResponse(HttpResponse response) {
        super(response, true, false);
        values = JsonUtilities.jsonToClass(getMessageBody(), List.class);
    }

    public List<VALUE> getValues() {
        return values;
    }

    @Override
    public CMResponseCode getResponseCode() {
        return CMResponseCode.codeForStatus(getStatusCode());
    }
}

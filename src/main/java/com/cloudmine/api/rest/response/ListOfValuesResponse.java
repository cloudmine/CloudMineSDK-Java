package com.cloudmine.api.rest.response;

import com.cloudmine.api.exceptions.CloudMineException;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.response.code.CMResponseCode;
import org.apache.http.HttpResponse;

import java.util.Collections;
import java.util.List;

/**
 * A response consisting of a list of some sort of values
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

            @Override
            public ListOfValuesResponse<VALUE> construct(String messageBody, int responseCode) throws CreationException {
                return new ListOfValuesResponse<VALUE>(messageBody, responseCode);
            }
        };
    }

    private List<VALUE> values;

    public ListOfValuesResponse(HttpResponse response) {
        super(response, true, false);
        try {
            values = JsonUtilities.jsonToClass(getMessageBody(), List.class);
        }catch (CloudMineException cme) {
            values = Collections.EMPTY_LIST;
        }
    }

    public ListOfValuesResponse(String msg, int responseCode) {
        super(msg, responseCode);
        try {
            values = JsonUtilities.jsonToClass(msg, List.class);
        }catch (CloudMineException cme) {
            values = Collections.EMPTY_LIST;
        }
    }

    public List<VALUE> getValues() {
        return values;
    }

    @Override
    public CMResponseCode getResponseCode() {
        return CMResponseCode.codeForStatus(getStatusCode());
    }
}

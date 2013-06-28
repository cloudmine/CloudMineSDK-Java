package com.cloudmine.api.rest.callbacks;

import com.cloudmine.api.rest.response.ListOfValuesResponse;

/**
 * A callback for {@link ListOfValuesResponse}
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class ListOfValuesResponseCallback<VALUE> extends CMCallback<ListOfValuesResponse<VALUE>> {

    public ListOfValuesResponseCallback() {
        super(ListOfValuesResponse.<VALUE>CONSTRUCTOR());
    }
}

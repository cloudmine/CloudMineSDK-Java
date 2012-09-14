package com.cloudmine.api.rest.callbacks;

import com.cloudmine.api.CMObject;
import com.cloudmine.api.rest.response.TypedCMObjectResponse;

/**
 * Callback for server calls that return a {@link com.cloudmine.api.rest.response.CMObjectResponse}
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class TypedCMObjectResponseCallback<CMO extends CMObject> extends CMCallback<TypedCMObjectResponse<CMO>> {

    public TypedCMObjectResponseCallback(Class<CMO> klass) {
        super(TypedCMObjectResponse.constructor(klass));

    }

    @Override
    public void onCompletion(TypedCMObjectResponse<CMO> response) {

    }
}

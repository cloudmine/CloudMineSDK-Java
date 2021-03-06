package com.cloudmine.api.rest.callbacks;


import com.cloudmine.api.rest.response.ObjectModificationResponse;

/**
 * Callback for server calls that return an {@link ObjectModificationResponse}, such as deleting or
 * inserting a {@link com.cloudmine.api.CMObject}
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class ObjectModificationResponseCallback extends CMCallback<ObjectModificationResponse> {
    public ObjectModificationResponseCallback() {
        super(ObjectModificationResponse.CONSTRUCTOR);
    }
}

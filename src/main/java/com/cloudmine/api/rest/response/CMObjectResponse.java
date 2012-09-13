package com.cloudmine.api.rest.response;

import com.cloudmine.api.CMObject;
import org.apache.http.HttpResponse;

/**
 *  Returned by the CloudMine service in response to object fetch requests. Provides access to the
 *  {@link CMObject}s returned by the request
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class CMObjectResponse extends TypedCMObjectResponse<CMObject> {

    public static final ResponseConstructor<CMObjectResponse> CONSTRUCTOR =
            new ResponseConstructor<CMObjectResponse>() {

                @Override
                public CMObjectResponse construct(HttpResponse response) {
                    return new CMObjectResponse(response);
                }
            };
    public static final String COUNT_KEY = "count";
    public static final int NO_COUNT = -1;


    public CMObjectResponse(HttpResponse response) {
        super(response, CMObject.class);
    }

    public CMObjectResponse(String response, int code) {
        super(response, code, CMObject.class);
    }
}

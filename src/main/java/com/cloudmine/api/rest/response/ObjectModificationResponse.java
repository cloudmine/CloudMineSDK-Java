package com.cloudmine.api.rest.response;

import org.apache.http.HttpResponse;

import java.util.concurrent.Future;

/**
 * Response returned whenever objects are PUT or POSTED to CloudMine
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 6/7/12, 3:20 PM
 */
public class ObjectModificationResponse extends SuccessErrorResponse{

    public static final ResponseConstructor<ObjectModificationResponse> CONSTRUCTOR = new ResponseConstructor<ObjectModificationResponse>() {
        @Override
        public ObjectModificationResponse construct(HttpResponse response) {
            return new ObjectModificationResponse(response);
        }

        @Override
        public Future<ObjectModificationResponse> constructFuture(Future<HttpResponse> futureResponse) {
            return createFutureResponse(futureResponse, this);
        }
    };
    public ObjectModificationResponse(HttpResponse response) {
        super(response);
    }

    public ObjectModificationResponse(String messageBody, int statusCode) {
        super(messageBody, statusCode);
    }

    public boolean wasUpdated(String key) {
        return ResponseValue.UPDATED.equals(keyResponse(key));
    }

    public boolean wasCreated(String key) {
        return ResponseValue.CREATED.equals(keyResponse(key));
    }

    public boolean wasDeleted(String key) {
        return ResponseValue.DELETED.equals(keyResponse(key));
    }

    public boolean wasModified(String key) {
        return !ResponseValue.MISSING.equals(keyResponse(key));
    }

    public ResponseValue keyResponse(String key) {
        Object keyedValue = successMap().get(key);
        if(keyedValue == null)
            return ResponseValue.MISSING;
        return ResponseValue.getValue(keyedValue.toString());
    }

}

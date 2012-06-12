package com.cloudmine.api.rest.response;

import org.apache.http.HttpResponse;

import java.util.concurrent.Future;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 6/4/12, 5:28 PM
 */
public class FileCreationResponse extends CMResponse {

    public static final ResponseConstructor<FileCreationResponse> CONSTRUCTOR = new ResponseConstructor<FileCreationResponse>() {
        @Override
        public FileCreationResponse construct(HttpResponse response) {
            return new FileCreationResponse(response);
        }

        @Override
        public Future<FileCreationResponse> constructFuture(Future<HttpResponse> futureResponse) {
            return createFutureResponse(futureResponse, this);
        }
    };

    public FileCreationResponse(HttpResponse response) {
        super(response);
    }

    public String fileKey() {
        Object key = getObject("key");
        if(key == null) {
            return null;
        }
        return key.toString();
    }
}

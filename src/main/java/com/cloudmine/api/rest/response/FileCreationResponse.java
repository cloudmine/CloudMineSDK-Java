package com.cloudmine.api.rest.response;

import org.apache.http.HttpResponse;

import java.util.concurrent.Future;

/**
 * Returned by the CloudMine service in response to file creation requests. Contains the fileName
 * for the created file
 * Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
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

    /**
     * Instantiate a new FileCreationResponse. You should probably not be calling this yourself
     * @param response a response to a file creation request
     */
    public FileCreationResponse(HttpResponse response) {
        super(response);
    }

    /**
     * Get the name for the file. May be null if the create request failed
     * @return The name for the file. May be null if the create request failed
     */
    public String getFileName() {
        Object key = getObject("key");
        if(key == null) {
            return null;
        }
        return key.toString();
    }
}

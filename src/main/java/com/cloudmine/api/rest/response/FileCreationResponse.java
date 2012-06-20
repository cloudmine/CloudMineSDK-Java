package com.cloudmine.api.rest.response;

import org.apache.http.HttpResponse;

import java.util.concurrent.Future;

/**
 * Returned by the CloudMine service in response to file creation requests. Contains the fileKey
 * for the created file
 * Copyright CloudMine LLC
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
     * Get the key for the file. May be null
     * @return The key for the file. May be null
     */
    public String getFileKey() {
        Object key = getObject("key");
        if(key == null) {
            return null;
        }
        return key.toString();
    }
}

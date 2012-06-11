package com.cloudmine.api.rest.callbacks;


import com.cloudmine.api.rest.response.FileCreationResponse;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 6/4/12, 5:32 PM
 */
public class FileCreationResponseCallback extends CMWebServiceCallback<FileCreationResponse> {
    public FileCreationResponseCallback() {
        super(FileCreationResponse.CONSTRUCTOR);
    }
}

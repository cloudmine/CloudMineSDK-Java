package com.cloudmine.api.rest.callbacks;

import com.cloudmine.api.rest.response.FileCreationResponse;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 6/4/12, 5:32 PM
 */
public class FileCreationResponseCallback extends CloudMineWebServiceCallback<FileCreationResponse> {
    public FileCreationResponseCallback() {
        super(FileCreationResponse.CONSTRUCTOR);
    }
}

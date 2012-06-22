package com.cloudmine.api.rest.callbacks;


import com.cloudmine.api.rest.response.FileCreationResponse;

/**
 * Callback for inserting CMFiles into cloudmine. Receives {@link FileCreationResponse}
 * Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class FileCreationResponseCallback extends CMCallback<FileCreationResponse> {
    public FileCreationResponseCallback() {
        super(FileCreationResponse.CONSTRUCTOR);
    }
}

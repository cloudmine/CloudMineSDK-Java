package com.cloudmine.api.rest.callbacks;

import com.cloudmine.api.rest.response.FileLoadResponse;

/**
 * Callback for when loading files from CloudMine. Receives CMFile
 * Copyright CloudMine LLC
 */
public class FileLoadCallback  extends CMCallback<FileLoadResponse> {
    public FileLoadCallback(String fileName) {
        super(FileLoadResponse.constructor(fileName));
    }
}

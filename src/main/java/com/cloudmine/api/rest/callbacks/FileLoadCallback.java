package com.cloudmine.api.rest.callbacks;

import com.cloudmine.api.rest.response.FileLoadResponse;

/**
 * Callback for when loading files from CloudMine. Receives CMFile
 * Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class FileLoadCallback  extends CMCallback<FileLoadResponse> {
    public FileLoadCallback(String fileName) {
        super(FileLoadResponse.constructor(fileName));
    }
}

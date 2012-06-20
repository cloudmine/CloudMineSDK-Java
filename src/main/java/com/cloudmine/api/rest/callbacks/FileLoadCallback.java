package com.cloudmine.api.rest.callbacks;

import com.cloudmine.api.CMFile;

/**
 * Callback for when loading files from CloudMine. Receives CMFile
 * Copyright CloudMine LLC
 */
public class FileLoadCallback  extends CMCallback<CMFile> {
    public FileLoadCallback(String fileName) {
        super(CMFile.constructor(fileName));
    }
}

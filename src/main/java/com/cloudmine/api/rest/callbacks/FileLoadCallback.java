package com.cloudmine.api.rest.callbacks;

import com.cloudmine.api.CMFile;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 6/4/12, 5:52 PM
 */
public class FileLoadCallback  extends CMWebServiceCallback<CMFile> {
    public FileLoadCallback(String key) {
        super(CMFile.constructor(key));
    }
}

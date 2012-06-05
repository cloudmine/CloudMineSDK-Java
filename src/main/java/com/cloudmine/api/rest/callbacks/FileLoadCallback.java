package com.cloudmine.api.rest.callbacks;

import com.cloudmine.api.CloudMineFile;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 6/4/12, 5:52 PM
 */
public class FileLoadCallback  extends CloudMineWebServiceCallback<CloudMineFile>  {
    public FileLoadCallback(String key) {
        super(CloudMineFile.constructor(key));
    }
}

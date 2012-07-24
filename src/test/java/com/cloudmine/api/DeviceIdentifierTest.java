package com.cloudmine.api;

import org.junit.Test;

import java.io.File;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class DeviceIdentifierTest {

    @Test
    public void testGetProperties() {
        deleteExistingIdFile();
        DeviceIdentifier id = new DeviceIdentifier();
        String deviceIdentifier = id.getUniqueId();
    }

    private void deleteExistingIdFile() {
        File propertiesFile = new File(DeviceIdentifier.PROPERTIES_FILE);
        try {
            propertiesFile.delete();
        }catch(Exception e) {
            //whatever
        }
    }
}

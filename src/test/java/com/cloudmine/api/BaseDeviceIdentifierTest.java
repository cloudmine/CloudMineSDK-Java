package com.cloudmine.api;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class BaseDeviceIdentifierTest {

    @Test
    public void testGetProperties() {
        deleteExistingIdFile();
        BaseDeviceIdentifier id = new BaseDeviceIdentifier();
        String deviceIdentifier = id.getUniqueId();
        assertNotNull(deviceIdentifier);

        id = new BaseDeviceIdentifier();
        String otherDeviceIdentifier = id.getUniqueId();
        assertEquals(deviceIdentifier, otherDeviceIdentifier);
    }

    private void deleteExistingIdFile() {
        File propertiesFile = new File(BaseDeviceIdentifier.PROPERTIES_FILE);
        try {
            propertiesFile.delete();
        }catch(Exception e) {
            //whatever
        }
    }
}

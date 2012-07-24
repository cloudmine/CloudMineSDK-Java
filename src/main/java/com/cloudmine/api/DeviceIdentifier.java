package com.cloudmine.api;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.io.*;
import java.util.Properties;
import java.util.UUID;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class DeviceIdentifier {

    public static final String UNIQUE_ID_KEY = "uniqueId";

    private static String uniqueId;
    public static final String DEVICE_HEADER_KEY = "X-CloudMine-UT";
    public static final String PROPERTIES_FILE = "properties";


    /**
     * Get the unique identifier for this Device and application
     * @return the unique identifier
     * @throws RuntimeException if initialize has not been called
     */
    public String getUniqueId() throws RuntimeException {
        if(uniqueId == null) {
            Properties properties = new Properties();
            loadPropertiesFile(properties);
            uniqueId = properties.getProperty(UNIQUE_ID_KEY);
            if(uniqueId == null) {
                uniqueId = generateUniqueDeviceIdentifier();

            }

        }
        return uniqueId;
    }

    private void loadPropertiesFile(Properties properties) {
        File idFile = new File(PROPERTIES_FILE);
        if(idFile.exists() && idFile.isFile() && idFile.canRead()) {
            try {
                FileInputStream reader = new FileInputStream(idFile);
                properties.load(reader);
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
            }
        }
    }

    /**
     * Get the header that should be included with any requests to cloudmine
     * @return the header that should be included with any requests to cloudmine
     * @throws RuntimeException if initialize has not been called
     */
    public Header getDeviceIdentifierHeader() throws RuntimeException {
        return new BasicHeader(DEVICE_HEADER_KEY, getUniqueId());
    }

    private String generateUniqueDeviceIdentifier() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}

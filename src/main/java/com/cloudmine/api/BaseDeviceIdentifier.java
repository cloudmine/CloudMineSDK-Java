package com.cloudmine.api;

import com.cloudmine.api.rest.HeaderFactory;
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
public class BaseDeviceIdentifier {

    public static final String UNIQUE_ID_KEY = "uniqueId";

    private String uniqueId;
    public static final String PROPERTIES_FILE = "cmPropertiesUUID";


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
                properties.setProperty(UNIQUE_ID_KEY, uniqueId);
                savePropertiesFile(properties);
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

    private void savePropertiesFile(Properties toSave) {
        File idFile = new File(PROPERTIES_FILE);
        try {
            FileOutputStream writer = new FileOutputStream(idFile);
            toSave.store(writer, "");
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
    }

    /**
     * Get the header that should be included with any requests to cloudmine
     * @return the header that should be included with any requests to cloudmine
     * @throws RuntimeException if initialize has not been called
     */
    public Header getDeviceIdentifierHeader() throws RuntimeException {
        return new BasicHeader(HeaderFactory.DEVICE_HEADER_KEY, getUniqueId());
    }

    private String generateUniqueDeviceIdentifier() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}

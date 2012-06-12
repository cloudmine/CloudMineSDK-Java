package com.cloudmine.api;

import com.cloudmine.api.exceptions.CreationException;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/16/12, 4:12 PM
 */
public class CMApiCredentials {
    private static final String HEADER_KEY = "X-CloudMine-ApiKey";

    private static final Immutable<CMApiCredentials> credentials = new Immutable<CMApiCredentials>();

    private final String applicationIdentifier;
    private final String applicationApiKey;

    /**
     * Sets the application id and api key. Can be called multiple times, but only the first call
     * @param id
     * @param apiKey
     * @throws CreationException if you try to initialize twice with different values, or null values were passed in
     * @return
     */
    public static synchronized CMApiCredentials initialize(String id, String apiKey) throws CreationException {
        if(id == null || apiKey == null) {
            throw new CreationException("Illegal null argument passed to initialize. Given id=" + id + " and apiKey=" + apiKey);
        }
        if(credentials.isSet()) {

            String currentApiKey = credentials.value().applicationApiKey;
            String currentIdentifier = credentials.value().applicationIdentifier;
            boolean valuesAreDifferent = !(id.equals(currentIdentifier) && apiKey.equals(currentApiKey));
            if(valuesAreDifferent) {
                throw new CreationException("Multiple calls mode to initialize with different values");
            }
        }
        credentials.setValue(new CMApiCredentials(id, apiKey));
        return credentials.value();
    }

    private CMApiCredentials(String id, String apiKey) {
        applicationIdentifier = id;
        applicationApiKey = apiKey;
    }

    public static CMApiCredentials credentials() {
        if(credentials.isSet() == false) {
            throw new CreationException("Cannot access CMApiCredentials before they have been initialized");
        }
        return credentials.value();
    }

    public static String applicationIdentifier() {
        return credentials().applicationIdentifier;
    }
    public static String applicationApiKey() {
        return credentials().applicationApiKey;
    }

    public static Header cloudMineHeader() {
        return new BasicHeader(HEADER_KEY, applicationApiKey());
    }
}

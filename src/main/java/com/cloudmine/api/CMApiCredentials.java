package com.cloudmine.api;

import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.persistance.CloudMineObjectAnnotationListener;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

/**
 * Singleton for defining your application identifier and application API key. Must be initialized before
 * any calls to the CloudMine API will succeed.
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class CMApiCredentials {
    private static final String HEADER_KEY = "X-CloudMine-ApiKey";

    private static final Immutable<CMApiCredentials> credentials = new Immutable<CMApiCredentials>();

    private final String applicationIdentifier;
    private final String applicationApiKey;

    /**
     * Sets the application id and api key. Can be called multiple times, but only the first call will modify the credentials value.
     * It is an error to call this multiple times with different values
     * @param id the application id from your CloudMine dashboard
     * @param apiKey the API key from your CloudMine dashboard
     * @throws CreationException if you try to initialize twice with different values, or null values were passed in
     * @return the initialized CMApiCredentials instance
     */
    public static synchronized CMApiCredentials initialize(String id, String apiKey) throws CreationException {
        //okay this is unrelated to initializing the api credentials but needs to happen before the API can be used so lets set it up
        CloudMineObjectAnnotationListener.runAnnotationDiscoverer();

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

    /**
     * Returns the CMApiCredentials that were created in {@link #initialize(String, String)}
     * @return the CMApiCredentials
     * @throws CreationException if called before the credentials have been initialized
     */
    public static CMApiCredentials getCredentials() throws CreationException {
        if(credentials.isSet() == false) {
            throw new CreationException("Cannot access CMApiCredentials before they have been initialized");
        }
        return credentials.value();
    }

    /**
     * Returns the application identifier
     * @return the application identifier
     */
    public static String getApplicationIdentifier() {
        return getCredentials().applicationIdentifier;
    }

    /**
     * Returns the application API key
     * @return the application API key
     */
    public static String getApplicationApiKey() {
        return getCredentials().applicationApiKey;
    }

    /**
     * Returns a Header that contains the CloudMine authentication information for a request
     * @return a Header that contains the CloudMine authentication information for a request
     */
    public static Header getCloudMineHeader() {
        return new BasicHeader(HEADER_KEY, getApplicationApiKey());
    }

}

package com.cloudmine.api;

import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.persistance.ClassNameRegistry;
import com.cloudmine.api.rest.CMURLBuilder;
import com.cloudmine.api.rest.HeaderFactory;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Singleton for defining your application identifier and application API key. Must be initialized before
 * any calls to the CloudMine API will succeed.
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class CMApiCredentials {
    private static final Logger LOG = LoggerFactory.getLogger(CMApiCredentials.class);


    private static CMApiCredentials credentials;

    private final String applicationIdentifier;
    private final String applicationApiKey;
    private final String baseUrl;

    static {
        ClassNameRegistry.register(JavaAccessListController.CLASS_NAME, JavaAccessListController.class);
        ClassNameRegistry.register(CMGeoPoint.GEOPOINT_CLASS, CMGeoPoint.class);
        ClassNameRegistry.register(JavaCMUser.CLASS_NAME, JavaCMUser.class);
    }


    /**
     * If you are using CloudMine on Android, this or {@link #initialize(String, String, Object)} is the initialize method you should be calling. Works just
     * like {@link #initialize(String, String, String)}, but sets some important android only information
     * @param id
     * @param apiKey
     * @param baseUrl the base CloudMine URL to use, if running on a non default CloudMine install
     * @param context either null if not running on android, or the value of getApplicationContext from your main activity. It isn't typed here so the Java sdk does not have any android dependencies
     * @return
     * @throws CreationException in addition to the reasons defined in {@link #initialize(String, String)}, also if you do not provide the application context and you're running on android
     */
    public static synchronized CMApiCredentials initialize(String id, String apiKey, String baseUrl, Object context) throws CreationException {
        if(Strings.isEmpty(id) || Strings.isEmpty(apiKey) || Strings.isEmpty(baseUrl)) {
            throw new CreationException("Illegal null/empty argument passed to initialize. Given id=" + id + " and apiKey=" + apiKey);
        }
        try {
            Class contextClass = Class.forName("android.content.Context");
            boolean invalidContext = context == null || contextClass == null || !contextClass.isAssignableFrom(context.getClass());
            if(invalidContext) {
                throw new CreationException("Running on android and application context not provided, try passing getApplicationContext to this method");
            }

            for(Method method : Class.forName("com.cloudmine.api.DeviceIdentifier").getMethods()) {
                if("initialize".equals(method.getName())) {
                    method.invoke(null, context);
                }
            }
        } catch (ClassNotFoundException e) {
            LOG.info("Not running on Android", e);
        } catch (InvocationTargetException e) {
            LOG.error("Exception thrown", e);
        } catch (IllegalAccessException e) {
            LOG.error("Exception thrown", e);
        }
        credentials = new CMApiCredentials(id, apiKey, baseUrl);
        return credentials;
    }


    /**
     * If you are using CloudMine on Android, this or {@link #initialize(String, String, String, Object)} is the initialize method you should be calling. Works just
     * like {@link #initialize(String, String)}, but sets some important android only information
     * @param id
     * @param apiKey
     * @param context either null if not running on android, or the value of getApplicationContext from your main activity. It isn't typed here so the Java sdk does not have any android dependencies
     * @return
     * @throws CreationException in addition to the reasons defined in {@link #initialize(String, String)}, also if you do not provide the application context and you're running on android
     */
    public static synchronized CMApiCredentials initialize(String id, String apiKey, Object context) throws CreationException {
        return initialize(id, apiKey, CMURLBuilder.CLOUD_MINE_URL, context);
    }

    /**
     * Sets the application id and api key. Can be called multiple times, but only the first call will modify the credentials value.
     * It is an error to call this multiple times with different values
     * @param id the application id from your CloudMine dashboard
     * @param apiKey the API key from your CloudMine dashboard
     * @throws CreationException if you try to initialize twice with different values, or null values were passed in
     * @return the initialized CMApiCredentials instance
     */
    public static synchronized CMApiCredentials initialize(String id, String apiKey) throws CreationException {
        return initialize(id, apiKey, CMURLBuilder.CLOUD_MINE_URL);
    }

    public static synchronized CMApiCredentials initialize(String id, String apiKey, String baseUrl) throws CreationException {
        credentials = new CMApiCredentials(id, apiKey, baseUrl);
        return credentials;
    }

    /**
     * Create a new instance of CMApiCredentials.
     * @param id
     * @param apiKey
     * @param baseUrl
     */
    public CMApiCredentials(String id, String apiKey, String baseUrl) {
        applicationIdentifier = id;
        applicationApiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    /**
     * Returns the CMApiCredentials that were created in {@link #initialize(String, String)}
     * @return the CMApiCredentials
     * @throws CreationException if called before the credentials have been initialized
     */
    public static CMApiCredentials getCredentials() throws CreationException {
        if(credentials == null) {
            throw new CreationException("Cannot access CMApiCredentials before they have been initialized. Please make a call to CMApiCredentials.initialize before attempting to make any CloudMine calls");
        }
        return credentials;
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
        return new BasicHeader(HeaderFactory.API_HEADER_KEY, getApplicationApiKey());
    }

    public String getIdentifier() {
        return applicationIdentifier;
    }

    public String getApiKey() {
        return applicationApiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}

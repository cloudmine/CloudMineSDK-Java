package com.cloudmine.test;

import com.cloudmine.api.*;
import com.cloudmine.api.persistance.ClassNameRegistry;
import com.cloudmine.api.rest.CMWebService;
import com.cloudmine.api.rest.Savable;
import com.cloudmine.api.rest.callbacks.CMObjectResponseCallback;
import com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback;
import com.cloudmine.api.rest.callbacks.ResponseBaseCallback;
import com.cloudmine.api.rest.response.CMObjectResponse;
import com.cloudmine.api.rest.response.ObjectModificationResponse;
import com.cloudmine.api.rest.response.ResponseBase;
import org.junit.Before;

import java.io.*;
import java.util.UUID;

import static com.cloudmine.test.AsyncTestResultsCoordinator.reset;
import static com.cloudmine.test.TestServiceCallback.testCallback;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 * User: johnmccarthy
 * Date: 6/14/12, 11:13 AM
 */
public class ServiceTestBase {
    private static final String APP_ID = "c1a562ee1e6f4a478803e7b51babe287";
    private static final String API_KEY = "27D924936D2C7D422D58B919B9F23653";
    protected static final String USER_PASSWORD = "test";
//    private static final String APP_ID = "94b48aea559b4bb6bd16e1d4a8469308";
//    private static final String API_KEY = "08cb0266f47840d28044d0e122286779";
    private static final CMUser user = new CMUser("tfjghkdfgjkdf@gmail.com", USER_PASSWORD);

    public static final TestServiceCallback hasSuccess = testCallback(new ResponseBaseCallback() {
        public void onCompletion(ResponseBase response) {
            assertTrue(response.wasSuccess());
        }
    });

    public static TestServiceCallback hasSuccessAndHasModified(final Savable... savables) {
        return testCallback(new ObjectModificationResponseCallback() {
           public void onCompletion(ObjectModificationResponse response) {
               assertTrue(response.wasSuccess());
               for(Savable saved : savables) {
                   assertTrue(response.wasModified(saved.getObjectId()));
               }
           }
        });
    }

    public static TestServiceCallback hasSuccessAndHasLoaded(final Savable... savables) {
        return testCallback(new CMObjectResponseCallback() {
           public void onCompletion(CMObjectResponse response) {
               assertTrue(response.wasSuccess());
               for(Savable loaded : savables) {
                   assertEquals(loaded, response.getCMObject(loaded.getObjectId()));
               }
           }
        });
    }

    public static String randomEmail() {
        return randomString() + "@gmail.com";
    }

    public static String randomString() {
        return UUID.randomUUID().toString();
    }

    public static CMUser randomUser() {
        return new CMUser(randomEmail(), randomString());
    }

    protected CMWebService service;
    @Before
    public void setUp() {
        ClassNameRegistry.register("govna", ExtendedCMObject.class);
        CMApiCredentials.initialize(APP_ID, API_KEY);
        service = CMWebService.getService();

        System.setProperty("org.slf4j.simplelogger.defaultlog", "debug");
        reset();

        user().setPassword(USER_PASSWORD);
        deleteAll();
    }

    private void deleteAll() {
        service.deleteAll();
        CMUser user = user();
        CMSessionToken token = service.login(user).getSessionToken();
        service.getUserWebService(token).deleteAll();
    }

    protected void deleteAllUsers() {
        service.asyncLoadAllUserProfiles(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                response.wasSuccess();
                for(CMObject object : response.getObjects()) {
                    if(object.hasObjectId()) {
                        service.asyncDeleteUser(object.getObjectId(), new ObjectModificationResponseCallback() {
                            public void onCompletion(ObjectModificationResponse response) {
                                if(response.wasSuccess()) {
                                    response.getDeletedObjectIds();
                                } else {
                                    response.getDeletedObjectIds();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    public CMUser user() {
        return new CMUser("tfjghkdfgjkdf@gmail.com", USER_PASSWORD);
    }

    public SimpleCMObject simpleUserObject() {
        SimpleCMObject object = simpleObject();
        object.setSaveWith(user());
        return object;
    }

    public SimpleCMObject simpleObject() {
        SimpleCMObject object = new SimpleCMObject();
        object.add("string", "value");
        object.add("bool", true);
        object.add("int", 5);
        return object;
    }


    public InputStream getObjectInputStream() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ObjectOutputStream objectOutput = new ObjectOutputStream(output);
        objectOutput.write(55);
        objectOutput.writeObject("Some String is Written");
        objectOutput.flush();
        objectOutput.close();

        return new ByteArrayInputStream(output.toByteArray());
    }
}

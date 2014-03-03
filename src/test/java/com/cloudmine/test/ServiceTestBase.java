package com.cloudmine.test;

import com.cloudmine.api.CMApiCredentials;
import com.cloudmine.api.CMObject;
import com.cloudmine.api.CMSessionToken;
import com.cloudmine.api.JavaCMUser;
import com.cloudmine.api.SimpleCMObject;
import com.cloudmine.api.persistance.ClassNameRegistry;
import com.cloudmine.api.rest.CMWebService;
import com.cloudmine.api.rest.Savable;
import com.cloudmine.api.rest.callbacks.CMObjectResponseCallback;
import com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback;
import com.cloudmine.api.rest.callbacks.ResponseBaseCallback;
import com.cloudmine.api.rest.response.CMObjectResponse;
import com.cloudmine.api.rest.response.LoginResponse;
import com.cloudmine.api.rest.response.ObjectModificationResponse;
import com.cloudmine.api.rest.response.ResponseBase;
import org.junit.Assert;
import org.junit.Before;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.UUID;

import static com.cloudmine.test.AsyncTestResultsCoordinator.reset;
import static com.cloudmine.test.AsyncTestResultsCoordinator.waitForTestResults;
import static com.cloudmine.test.TestServiceCallback.testCallback;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 * User: johnmccarthy
 * Date: 6/14/12, 11:13 AM
 */
public class ServiceTestBase {
//    public static final String APP_ID = "c1a562ee1e6f4a478803e7b51babe287";
//    public static final String API_KEY = "27D924936D2C7D422D58B919B9F23653";
    public static final String APP_ID = "f8edcd61af8b434a843c4f08fcabe78e";
    public static final String API_KEY = "373395dadf514f7da47708c4047edecb";
    protected static final String USER_PASSWORD = "test";
    private static final JavaCMUser user = new JavaCMUser("tfjghkdfgjkdf@gmail.com", USER_PASSWORD);

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

    public static JavaCMUser randomUser() {
        return new JavaCMUser(randomEmail(), "test");
    }

    protected CMWebService service;
    @Before
    public void setUp() {
        ClassNameRegistry.register("govna", ExtendedCMObject.class);
        CMApiCredentials.initialize(APP_ID, API_KEY);
        service = CMWebService.getService();

        System.setProperty("org.slf4j.simplelogger.defaultlog", "debug");
        reset();

        deleteAll();
//        deleteAllUsers();
    }

    private void deleteAll() {
        service.deleteAll();
        JavaCMUser user = user();

        CMSessionToken token = service.login(user).getSessionToken();
        service.getUserWebService(token).deleteAll();
    }

    protected void deleteAllUsers() {
        service.asyncLoadAllUserProfiles(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                response.wasSuccess();
                List<CMObject> objects = response.getObjects();
                reset(objects.size());
                for(CMObject object : objects) {
                    if(object.hasObjectId()) {
                        System.out.println("Deleting: " + object.getObjectId());
//                        reset();
                        service.asyncDeleteUser(object.getObjectId(), testCallback());
//                        waitForTestResults();
                    }
                }
                waitForTestResults(500000);
                JavaCMUser user = user();
                user.createUser(hasSuccess);
                waitForTestResults();
            }
        });
    }

    public static void main(String... args) {
        ServiceTestBase serviceTestBase = new ServiceTestBase();
        serviceTestBase.setUp();
        serviceTestBase.deleteAllUsers();
    }

    public JavaCMUser user() {
        return new JavaCMUser("tfjghkdfgjkdf@gmail.com", USER_PASSWORD);
    }

    public JavaCMUser loggedInUser() {
        CMWebService.getService().insert(user);
        return loggedInUser(user);
    }

    public JavaCMUser randomLoggedInUser() {
        JavaCMUser randomUser = randomUser();
        assertTrue(service.insert(randomUser).wasSuccess());
        return loggedInUser(randomUser);
    }

    public JavaCMUser loggedInUser(JavaCMUser user) {
        LoginResponse response = CMWebService.getService().login(user);
        Assert.assertTrue(response.wasSuccess());
        Assert.assertTrue(response.getSessionToken().isValid());
        user.setSessionToken(response.getSessionToken());
        return user;
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


    public InputStream getObjectInputStream() {
        try {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ObjectOutputStream objectOutput = new ObjectOutputStream(output);
        objectOutput.write(55);
        objectOutput.writeObject("Some String is Written");
        objectOutput.flush();
        objectOutput.close();

        return new ByteArrayInputStream(output.toByteArray());
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}

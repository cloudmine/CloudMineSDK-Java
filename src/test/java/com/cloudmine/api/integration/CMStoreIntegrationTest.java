package com.cloudmine.api.integration;

import com.cloudmine.api.*;
import com.cloudmine.api.rest.CMFileMetaData;
import com.cloudmine.api.rest.CMStore;
import com.cloudmine.api.rest.CMWebService;
import com.cloudmine.api.rest.UserCMWebService;
import com.cloudmine.api.rest.callbacks.*;
import com.cloudmine.api.rest.options.*;
import com.cloudmine.api.rest.response.*;
import com.cloudmine.api.rest.response.code.FileLoadCode;
import com.cloudmine.api.rest.response.code.LoginCode;
import com.cloudmine.api.rest.response.code.ObjectLoadCode;
import com.cloudmine.api.rest.response.code.ObjectModificationCode;
import com.cloudmine.test.AsyncTestResultsCoordinator;
import com.cloudmine.test.ExtendedCMObject;
import com.cloudmine.test.ExtendedCMUser;
import com.cloudmine.test.ServiceTestBase;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.cloudmine.test.AsyncTestResultsCoordinator.reset;
import static com.cloudmine.test.AsyncTestResultsCoordinator.waitThenAssertTestResults;
import static com.cloudmine.test.TestServiceCallback.testCallback;
import static junit.framework.Assert.*;

/**
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 * User: johnmccarthy
 * Date: 6/13/12, 3:51 PM
 */

public class CMStoreIntegrationTest extends ServiceTestBase {
    private CMStore store;

    @Before
    public void setUp() {
        super.setUp();
        store = CMStore.CMStore();
    }

    @Test
    public void testSaveObject() {
        final SimpleCMObject object = new SimpleCMObject();
        object.add("bool", true);


        store.saveObject(object, testCallback(new ObjectModificationResponseCallback() {
            public void onCompletion(ObjectModificationResponse response) {

                CMObjectResponse loadResponse = CMWebService.getService().loadObject(object.getObjectId());
                SimpleCMObject loadedObject = (SimpleCMObject)loadResponse.getCMObject(object.getObjectId());
                assertNotNull(loadedObject);

                assertEquals(object, loadedObject);
                assertEquals(StoreIdentifier.DEFAULT, object.getSavedWith());

            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testSaveUserObject() throws ExecutionException, InterruptedException {
        final SimpleCMObject object = new SimpleCMObject();
        object.add("bool", true);
        final CMUser user = new CMUser("dfljdsfkdfskd@t.com", "t");
        CMWebService.getService().insert(user);


        object.setSaveWith(StoreIdentifier.StoreIdentifier(user));
        CMStore store = CMStore.CMStore();
        store.setUser(user);

        store.saveObject(object, testCallback(new ObjectModificationResponseCallback() {
            public void onCompletion(ObjectModificationResponse ignoredResponse) {
                assertTrue(ignoredResponse.wasSuccess());
                final CMSessionToken token = CMWebService.getService().login(user).getSessionToken();
                CMObjectResponse response = CMWebService.getService().getUserWebService(token).loadObject(object.getObjectId());
                assertTrue(response.wasSuccess());
                SimpleCMObject loadedObject = (SimpleCMObject)response.getCMObject(object.getObjectId());
                assertNotNull(loadedObject);
                assertEquals(object, loadedObject);
            }
        }));
        waitThenAssertTestResults();
        user.login(hasSuccess);
        waitThenAssertTestResults();
        CMSessionToken token = user.getSessionToken();
        CMObjectResponse response = CMWebService.getService().getUserWebService(token).loadObject(object.getObjectId());
        assertTrue(response.wasSuccess());
        assertEquals(response.getCMObject(object.getObjectId()), object);
    }

    @Test
    public void testSearchObjects() {
        final SimpleCMObject object = new SimpleCMObject();
        object.add("searchable", "value");
        object.save(hasSuccess);
        waitThenAssertTestResults();

        CMStore.getStore().loadApplicationObjectsSearch("[searchable=\"value\"]", testCallback(new CMObjectResponseCallback() {
            @Override
            public void onCompletion(CMObjectResponse response) {
                assertTrue(response.wasSuccess());

                assertEquals(object, response.getCMObject(object.getObjectId()));
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testSearchGeoPoint() {

        final SimpleCMObject object = new SimpleCMObject();
        object.add("name", "John");
        object.add("age", 25);
        CMGeoPoint location = new CMGeoPoint(55, 50);
        object.add("location", location);
        ObjectModificationResponse response = CMWebService.getService().insert(object.transportableRepresentation());
        assertTrue(response.wasSuccess());

        CMStore.getStore().loadApplicationObjectsSearch("[location near (50, 48)]", testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                assertTrue(response.wasSuccess());
                CMObject loadedObject = response.getCMObject(object.getObjectId());
//                assertEquals(object, loadedObject); //this is broken until subobjects on simplecmobjects are fixed
                assertNotNull(loadedObject);
                Distance distance = response.getDistanceFor(loadedObject.getObjectId());
                assertNotNull(distance);
            }
        }), new CMRequestOptions(new CMSearchOptions(DistanceUnits.km)));
        waitThenAssertTestResults();
    }

    @Test
    public void testUserLogin() {
        CMUser user = user();
        service.insert(user);
        CMSessionToken token = service.login(user).getSessionToken();
        service.getUserWebService(token).insert(new SimpleCMObject("key").add("k", "v").transportableRepresentation());
        reset(2);
        store.login(user, testCallback(new LoginResponseCallback() {
            public void onCompletion(LoginResponse response) {
                assertTrue(response.wasSuccess());
                store.loadAllUserObjects(testCallback(new CMObjectResponseCallback() {
                    public void onCompletion(CMObjectResponse response) {
                        assertTrue(response.wasSuccess());
                        SimpleCMObject object = response.getCMObject("key", SimpleCMObject.class);
                        assertEquals("v", object.getString("k"));
                    }
                }));
            }
        }));
        waitThenAssertTestResults();
        store.login(new CMUser("dontexist@nowhere.net", "sp"), testCallback(new LoginResponseCallback() {
            public void onCompletion(LoginResponse response) {
                assertEquals(LoginCode.MISSING_OR_INVALID_AUTHORIZATION, response.getResponseCode());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testLoadUserObjects() {
        SimpleCMObject appObject = new SimpleCMObject();
        appObject.add("SomeKey", "Value");


        service.insert(appObject.transportableRepresentation());

        final CMUser user = user();
        service.insert(user);
        final CMSessionToken token = service.login(user).getSessionToken();

        final List<SimpleCMObject> userObjects = new ArrayList<SimpleCMObject>();
        UserCMWebService userService = service.getUserWebService(token);
        for(int i = 0; i < 5; i++) {
            SimpleCMObject userObject = new SimpleCMObject();
            userObject.add("integer", i);
            userObjects.add(userObject);
            userService.insert(userObject.transportableRepresentation());
        }
        store.setUser(user);
        store.loadAllUserObjects(testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                assertTrue(response.wasSuccess());
                for (SimpleCMObject object : userObjects) {
                    SimpleCMObject responseObject = (SimpleCMObject)response.getCMObject(object.getObjectId());
                    assertEquals(StoreIdentifier.StoreIdentifier(user), responseObject.getSavedWith());
                    assertEquals(object, responseObject);
                }
            }
        }), new CMRequestOptions(CMPagingOptions.ALL_RESULTS));
        waitThenAssertTestResults();
    }

    @Test
    public void testLoadUserObjectsOfClass() {
        SimpleCMObject object = simpleObject();
        object.setClass("testObject");
        CMUser user = user();
        object.setSaveWith(user);
        object.save(testCallback(new ObjectModificationResponseCallback() {
            public void onCompletion(ObjectModificationResponse response) {
                assertTrue(response.wasSuccess());
            }
        }));

        waitThenAssertTestResults();
        user.logout(testCallback(new CMResponseCallback() {
            @Override
            public void onCompletion(CMResponse response) {
                assertTrue(response.wasSuccess());
            }
        }));
        waitThenAssertTestResults(20);
        store.setUser(user);
        user.setPassword(USER_PASSWORD);
        store.loadUserObjectsOfClass("testObject", hasSuccessAndHasLoaded(object));
        waitThenAssertTestResults();
    }

    @Test
    public void testLoadUserProfilesSearch() {

        int numberOfUsers = 5;
        reset(numberOfUsers);
        final List<ExtendedCMUser> expectedLoadedUsers = new ArrayList<ExtendedCMUser>();
        for(int ageMultiplier = 0; ageMultiplier < numberOfUsers; ageMultiplier++) {
            ExtendedCMUser user = new ExtendedCMUser(randomEmail(), randomString());
            user.setAge(ageMultiplier * 10);
            user.save(testCallback());
            if(user.getAge() < 30) {
                expectedLoadedUsers.add(user);
            }
        }
        assertEquals(3, expectedLoadedUsers.size());
        waitThenAssertTestResults();

        store.loadUserProfilesSearch("[age < 30]", new CMRequestOptions(CMPagingOptions.ALL_RESULTS), testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                assertTrue(response.wasSuccess());
                for (CMObject expectedUser : expectedLoadedUsers) {
                    assertEquals(expectedUser, response.getCMObject(expectedUser.getObjectId()));
                }
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testLoadSortOrder() {
        store.addObjects(Arrays.asList(new ExtendedCMObject(1), new ExtendedCMObject(2), new ExtendedCMObject(5), new ExtendedCMObject(4), new ExtendedCMObject(3)));
        store.addObjects(Arrays.asList(new ExtendedCMObject("Annie", 100), new ExtendedCMObject("Betty", 100), new ExtendedCMObject("Fred", 100)));
        store.saveStoreApplicationObjects(testCallback(new ObjectModificationResponseCallback() {
            public void onCompletion(ObjectModificationResponse response) {
                assertTrue(response.wasSuccess());
            }
        }));
        waitThenAssertTestResults();

        CMRequestOptions options = new CMRequestOptions(new CMSortOptions.Builder().addSortField("number", CMSortOptions.SortDirection.ASCENDING)
                                                                .addSortField("name", CMSortOptions.SortDirection.DESCENDING).build());
        store.loadAllApplicationObjects(testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                assertTrue(response.wasSuccess());
                int[] expectedNumbers = {1, 2, 3, 4, 5, 100, 100, 100};
                String[] expectedNames = {"Fred", "Betty", "Annie"};
                int expectedNumberCounter = 0;
                int expectedNamesCounter = 0;
                for(ExtendedCMObject extendedCMObject : response.getObjects(ExtendedCMObject.class)) {

                    int number = extendedCMObject.getNumber();
                    assertEquals(expectedNumbers[expectedNumberCounter], number);
                    if(number == 100) {
                        assertEquals(expectedNames[expectedNamesCounter], extendedCMObject.getName());
                        expectedNamesCounter++;
                    }
                    expectedNumberCounter++;
                }
                assertEquals(8, expectedNumberCounter);
            }
        }), options);
        waitThenAssertTestResults();
    }

    @Test
    public void testLoadClassAndSearchQueryTyped() {
        ExtendedCMObject object = new ExtendedCMObject(10);
        ExtendedCMObject notLoadedObject = new ExtendedCMObject(20);
        store.addObject(object);
        store.addObject(notLoadedObject);
        store.saveStoreApplicationObjects(hasSuccess);
        waitThenAssertTestResults();

        store.loadApplicationObjectsOfClass(ExtendedCMObject.class,
                testCallback(new CMObjectResponseCallback() {
                    @Override
                    public void onCompletion(CMObjectResponse response) {
                        assertTrue(response.wasSuccess());
                        int loaded = 0;
                        for (ExtendedCMObject object : response.getObjects(ExtendedCMObject.class)) {
                            assertTrue(object.getNumber() < 21);
                            loaded++;
                        }
                        assertEquals(2, loaded);
                    }
                }));
        waitThenAssertTestResults();
        //okay now test the multi search
        store.loadApplicationObjectsOfClassWithSearch(ExtendedCMObject.class, "[number < 20]", testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                assertTrue(response.wasSuccess());
                assertEquals(1, response.getObjects().size());
                assertEquals(10, response.getObjects(ExtendedCMObject.class).get(0).getNumber());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testDeleteObjects() {
        final SimpleCMObject appObject = new SimpleCMObject();
        appObject.add("SomeKey", "Value");
        service.insert(appObject.transportableRepresentation());

        store.deleteObject(appObject, testCallback(new ObjectModificationResponseCallback() {
            public void onCompletion(ObjectModificationResponse response) {
                assertTrue(response.wasSuccess());
                response.wasDeleted(appObject.getObjectId());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testSaveStoreUserObjects() {
        SimpleCMObject object = simpleUserObject();
        store.addObject(object);
        store.setUser(user());
        store.saveStoreUserObjects(hasSuccessAndHasModified(object));
        waitThenAssertTestResults();
    }

    @Test
    public void testDeleteUserObject() {
        final SimpleCMObject userObject = new SimpleCMObject();
        userObject.add("key", "value");

        CMUser user = user();

        userObject.setSaveWith(user);
        store.setUser(user);
        store.saveObject(userObject, testCallback(new ObjectModificationResponseCallback() {
            public void onCompletion(ObjectModificationResponse response) {
                assertTrue(response.wasSuccess());
                assertTrue(response.wasCreated(userObject.getObjectId()));
            }
        }));
        waitThenAssertTestResults();
        assertEquals(1, store.getStoredObjects().size());
        store.deleteObject(userObject, testCallback(new ObjectModificationResponseCallback() {
            public void onCompletion(ObjectModificationResponse response) {
                assertTrue(response.wasSuccess());
                assertTrue(response.wasDeleted(userObject.getObjectId()));
            }
        }));
        waitThenAssertTestResults();
        assertEquals(0, store.getStoredObjects().size());
    }

    @Test
    public void testInvalidCredentialsUserOperation() {
        store.setUser(new CMUser("xnnxNOEXISTMANxnxnx@hotmail.com", "t"));
        final Immutable<Boolean> wasEntered = new Immutable<Boolean>();
        AsyncTestResultsCoordinator.ignoreOnFailure(true);
        store.loadUserObjectsOfClass("whatever", testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                assertEquals(ObjectLoadCode.MISSING_OR_INVALID_CREDENTIALS, response.getResponseCode());
            }

            @Override
            public void onFailure(Throwable t, String message) {
                wasEntered.setValue(Boolean.TRUE);
            }
        }));
        waitThenAssertTestResults();
        AsyncTestResultsCoordinator.ignoreOnFailure(false);
        assertTrue(wasEntered.value());
    }

    @Test
    public void testCreateAndLoadUserFile() throws IOException {
        final CMUser user = user();
        final CMFile file = new CMFile(getObjectInputStream());
        file.setSaveWith(user);
        store.setUser(user);

        store.saveFile(file, hasSuccess);
        waitThenAssertTestResults();
        user.logout(hasSuccess);
        user.setPassword(USER_PASSWORD);
        waitThenAssertTestResults();
        store.loadUserFile(file.getFileId(), testCallback(new FileLoadCallback(file.getFileId()) {
            public void onCompletion(FileLoadResponse response) {
                assertTrue(response.wasSuccess());
                assertEquals(file, response.getFile());
            }
        }));
        waitThenAssertTestResults();
        store.loadUserFileMetaData(file.getFileId(), testCallback(new CMObjectResponseCallback() {
            @Override
            public void onCompletion(CMObjectResponse response) {
                List<CMFileMetaData> metaData = response.getObjects(CMFileMetaData.class);
                assertEquals(1, metaData.size());
                assertEquals(file.getFileId(), metaData.get(0).getFilename());
            }
        }));
        waitThenAssertTestResults();
        store.loadUserFile("thisFileDoesNotevenexistMAN", testCallback(new FileLoadCallback("thisFileDoesNotevenexistMan") {
            public void onCompletion(FileLoadResponse response) {
                assertEquals(FileLoadCode.APPLICATION_ID_OR_FILE_NOT_FOUND, response.getResponseCode());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testDeleteUserFile() throws IOException {
        final CMUser user = user();
        final CMFile file = new CMFile(getObjectInputStream());

        file.setSaveWith(user);
        file.save(hasSuccess);
        waitThenAssertTestResults();
        store.setUser(user);
        store.deleteUserFile(file.getFileId(), testCallback(new ObjectModificationResponseCallback() {
            public void onCompletion(ObjectModificationResponse response) {
                assertTrue(response.wasSuccess());
                assertTrue(response.wasDeleted(file.getFileId()));
                assertEquals(ObjectModificationCode.SUCCESS, response.getResponseCode());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testSaveAddedObjects() throws ExecutionException, TimeoutException, InterruptedException {
        SimpleCMObject appObject = new SimpleCMObject();
        appObject.setSaveWith(StoreIdentifier.DEFAULT);
        appObject.add("simple", "value");

        store.addObject(appObject);
        store.saveStoreApplicationObjects(hasSuccessAndHasModified(appObject));
        waitThenAssertTestResults();
    }

    @Test
    public void testStoreKeepValues() throws ExecutionException, TimeoutException, InterruptedException {
        final SimpleCMObject object = simpleObject();

        store.saveObject(object, testCallback());
        waitThenAssertTestResults();
        object.add("number", 10);
        object.add("string", "name");
        object.setClass("testObject");

        store.saveStoreApplicationObjects(hasSuccessAndHasModified(object));
        waitThenAssertTestResults();
        store.loadAllApplicationObjects(testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse loadResponse) {
                assertTrue(loadResponse.wasSuccess());
                CMObject loadObject = loadResponse.getCMObject(object.getObjectId());
                assertEquals(object, loadObject);
                assertEquals(object, store.getStoredObject(object.getObjectId()));
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testStoreFunctionOptions() throws ExecutionException, TimeoutException, InterruptedException {
        SimpleCMObject object = simpleObject();
        object.add("string", "dog");
        store.saveObject(object, hasSuccess);
        waitThenAssertTestResults();
        CMServerFunction function = new CMServerFunction("NewSnippet", false);
        store.loadApplicationObjectWithObjectId(object.getObjectId(), testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                Object result = response.getObject("result");
                assertNotNull(result);
            }
        }),
                CMRequestOptions.CMRequestOptions(function));
        waitThenAssertTestResults();
    }

    @Test
    public void testStorePagingOptions() throws ExecutionException, TimeoutException, InterruptedException {
        Collection<SimpleCMObject> objects = new ArrayList<SimpleCMObject>();
        for(int i = 0; i < 5; i++) {
            objects.add(simpleObject());
        }
        store.addObjects(objects);
        store.saveStoreApplicationObjects(hasSuccess);
        waitThenAssertTestResults();
        CMRequestOptions options = CMRequestOptions.CMRequestOptions(CMPagingOptions.CMPagingOptions(2, 0, true));
        store.loadAllApplicationObjects(testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse loadResponse) {
                assertTrue(loadResponse.wasSuccess());

                assertEquals(2, loadResponse.getObjects().size());
                assertTrue(loadResponse.getCount() >= 5);
            }
        }), options);
        waitThenAssertTestResults();
    }

    @Test
    public void testLoadAllUsers() {
        final List<CMUser> users = new ArrayList<CMUser>();
        for(String userName : new String[]{"fred", "liz", "sarah"}) {
            CMUser user = new CMUser(userName + "@gmail.com", "password");
            users.add(user);
            CMWebService.getService().insert(user);
        }
        store.loadAllUserProfiles(testCallback(new CMObjectResponseCallback() {
            @Override
            public void onCompletion(CMObjectResponse response) {
                assertTrue(response.wasSuccess());
                assertTrue(response.getObjects().size() >= 3);
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testLoadLoggedInUser() {
        final CMUser user = user();
        service.insert(user);
        user.login(hasSuccess);
        waitThenAssertTestResults();
        store.setUser(user);
        store.loadLoggedInUserProfile(testCallback(new CMObjectResponseCallback() {
            @Override
            public void onCompletion(CMObjectResponse response) {
                assertTrue(response.wasSuccess());
//                assertEquals(user, response.getObjects().get(0)); //TODO once CMUsers work correctly with deserialization we can fix this
            }
        }));
        waitThenAssertTestResults();
    }
}

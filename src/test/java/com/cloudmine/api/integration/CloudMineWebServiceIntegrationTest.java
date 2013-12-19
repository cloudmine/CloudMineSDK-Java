package com.cloudmine.api.integration;

import com.cloudmine.api.*;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.callbacks.*;
import com.cloudmine.api.rest.response.*;
import com.cloudmine.test.ServiceTestBase;
import junit.framework.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.cloudmine.test.AsyncTestResultsCoordinator.*;
import static com.cloudmine.test.TestServiceCallback.testCallback;
import static junit.framework.Assert.*;

/**
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 * CMUser: johnmccarthy
 * Date: 5/16/12, 2:40 PM
 */
public class CloudMineWebServiceIntegrationTest extends ServiceTestBase{

    private static final String TEST_JSON = "{\"TESTING4703\": [\"value1\", \"value2\"]}";
    private static final String DEEP_KEYED_JSON =             "    \"deepKeyed\": {\n" +
            "        \"innerKey\":\"inner spaced String\",\n" +
            "        \"innerKeyToNumber\":45,\n" +
            "        \"anotherObject\": {\n" +
            "            \"innerObjectKey\":[1, 2, 3, 4, 5]\n" +
            "        }\n" +
            "    }\n";
    private static final String SIMPLE_JSON = " \"oneKey\":{ \"meaningOfLife\":42}";
    private static final String COMPLEX_JSON = "{\n" +
           SIMPLE_JSON + ",\n" +
            DEEP_KEYED_JSON +
            "}";
    public static final CMUser USER = new CMUser("francis2@gmail.com", "GOD");




    @Test
    @Ignore //Only works when we can delete users
    public void testAsyncCreateUser() throws Exception {
        CMUser newUser = new CMUser("test2@test.com", "password");
        service.asyncCreateUser(newUser, testCallback(new CreationResponseCallback() {
            @Override
            public void onCompletion(CreationResponse response) {
                Assert.assertTrue(response.was(201));
            }

            @Override
            public void onFailure(Throwable ex, String content) {
                ex.printStackTrace();
                Assert.fail("Failed");
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testBasicSet() {
        ObjectModificationResponse response = service.insert(TEST_JSON);
        assertWasSuccess(response);

        Assert.assertTrue(response.hasSuccessKey("TESTING4703"));
    }

    @Test
    public void testStringSearch() {
        service.insert(COMPLEX_JSON);

        CMObjectResponse response = service.loadSearch("[innerKey=\"inner spaced String\"]");
        assertWasSuccess(response);
    }

    @Test
    public void testDeleteAll() {
        service.insert(TEST_JSON);
        ObjectModificationResponse response = service.deleteAll();
        assertWasSuccess(response);
    }

    @Test
    public void testBasicUpdate() {
        ObjectModificationResponse response = service.update(TEST_JSON);
        assertWasSuccess(response);

        Assert.assertTrue(response.hasSuccessKey("TESTING4703"));
    }

    @Test
    public void testBasicGet() {
        service.insert(TEST_JSON);
        CMObjectResponse response = service.loadAllObjects();
        assertWasSuccess(response);
        Assert.assertTrue(response.hasSuccessKey("TESTING4703"));
    }

    @Test
    public void testFileStorageSet() throws Exception {
        InputStream input = getObjectInputStream();
        ResponseBase response = service.insert(new CMFile(input));
        Assert.assertNotNull(response);
        Assert.assertTrue(response.hasObject("key"));
    }

    @Test
    public void testFileStorageGet() throws Exception {
        CMFile insertedFile = new CMFile(getObjectInputStream(), "theFileKey", CMFile.DEFAULT_CONTENT_TYPE);
        ResponseBase response = service.insert(
                insertedFile);

        FileLoadResponse loadedFileResponse = service.loadFile("theFileKey");
        assertTrue(loadedFileResponse.wasSuccess());
        CMFile loadedFile = loadedFileResponse.getFile();
        byte[] insertedFileContents = insertedFile.getFileContents();
        byte[] loadedFileContents = loadedFile.getFileContents();
        assertEquals(insertedFileContents.length, loadedFileContents.length);
        for(int i = 0; i < loadedFileContents.length; i++) {
            assertEquals(insertedFileContents[i], loadedFileContents[i]);
        }
    }

    @Test
    public void testAsyncObjectLoad() throws Throwable {
        final SimpleCMObject task = new SimpleCMObject();
        task.setClass("task");
        task.add("name", "Do dishes");
        task.add("isDone", false);
        reset(2);
        service.asyncInsert(task, testCallback(new ObjectModificationResponseCallback() {
            @Override
            public void onCompletion(ObjectModificationResponse response) {
                service.asyncLoadObjectsOfClass("task", testCallback(new CMObjectResponseCallback() {
                    public void onCompletion(CMObjectResponse objectResponse) {
                        Assert.assertEquals(1, objectResponse.getObjects().size());
                        Assert.assertEquals(task, objectResponse.getObjects().get(0));
                    }
                }));
                assertWasSuccess(response);
            }

            @Override
            public void onFailure(Throwable error, String message) {
                error.printStackTrace();
                Assert.fail("failed! " + message);
            }
        }));
        waitForTestResults();
        assertAsyncTaskResult();
    }

    @Test
    public void testKeyedDelete() {
        service.insert(COMPLEX_JSON);

        SuccessErrorResponse response = service.delete("deepKeyed");

        assertWasSuccess(response);

        response = service.loadAllObjects();
        assertWasSuccess(response);
        Assert.assertTrue(response.hasSuccessKey("oneKey"));
        Assert.assertFalse(response.hasSuccessKey("deepKeyed"));

        service.insert(COMPLEX_JSON);

        service.delete(Arrays.asList("deepKeyed", "oneKey"));
        response = service.loadAllObjects();
        Assert.assertFalse(response.hasSuccessKey("oneKey"));
        Assert.assertFalse(response.hasSuccessKey("deepKeyed"));
    }

    @Test
    @Ignore // until we can delete users this test will fail every time but the first time its run
    public void testCreateUser() {
        CMResponse response = service.insert(USER);
        Assert.assertTrue(response.was(201));
    }

    @Test
    public void testUserLogin() {
        CMUser nonExistentUser = new CMUser("some@dude.com", "123");
        LoginResponse response = service.login(nonExistentUser);
        Assert.assertTrue(response.was(401));
        service.insert(USER);

        response = service.login(USER);
        Assert.assertTrue(response.was(200));
    }

    @Test
    public void testUserLogout() {
        CMResponse response = service.logout(new CMSessionToken("this token doesn't exist", new Date()));
        Assert.assertEquals(401, response.getStatusCode());

        service.insert(USER);
        LoginResponse loginResponse = service.login(USER);
        Assert.assertTrue(loginResponse.was(200));

        response = service.logout(loginResponse.getSessionToken());
        Assert.assertTrue(response.was(200));
    }

    @Test
    public void testAsyncChangePassword() {
        CMUser user = randomUser();
        service.insert(user);
        service.asyncChangePassword(user, "newPassword", testCallback(new CMResponseCallback() {
            public void onCompletion(CMResponse response) {
                assertTrue(response.wasSuccess());
            }
        }));
        waitThenAssertTestResults();
        CMUser newPasswordUser = new CMUser(user.getEmail(), "newPassword");
        LoginResponse response = service.login(newPasswordUser);
        assertTrue(response.wasSuccess());
    }

    @Test
    public void testAsyncResetPassword() {
        CMUser user = randomUser();
        service.insert(user);

        service.asyncResetPasswordRequest(user.getEmail(), testCallback(new CMResponseCallback() {
            @Override
            public void onCompletion(CMResponse response) {
                assertTrue(response.wasSuccess());
            }
        }));//can't actually test the code since it goes to an email address
        waitThenAssertTestResults();
    }

    @Test
    public void testAsyncLogin() {
        service.insert(USER);
        service.asyncLogin(USER, testCallback(new LoginResponseCallback() {
            @Override
            public void onCompletion(LoginResponse response) {
                Assert.assertTrue(response.wasSuccess());
            }
        }));
        waitThenAssertTestResults();
        service.asyncLogin(new CMUser("thisdoesntexist@dddd.com", "somepass"), testCallback(new LoginResponseCallback() {
            public void onCompletion(LoginResponse response) {
                Assert.assertEquals(CMSessionToken.FAILED, response.getSessionToken());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testAsyncLogout() {
        service.insert(USER);
        reset(2);
        service.asyncLogin(USER, testCallback(new LoginResponseCallback() {
            public void onCompletion(LoginResponse response) {
                Assert.assertTrue(response.wasSuccess());

                service.asyncLogout(response.getSessionToken(), testCallback(new CMResponseCallback() {
                    public void onCompletion(CMResponse response) {
                        Assert.assertTrue(response.wasSuccess());
                    }
                }));
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testAsyncGet() {
        service.insert(COMPLEX_JSON);
        service.asyncLoadObjects(testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                Assert.assertTrue(response.getObjects().size() >= 2);
                CMObject object = response.getCMObject("deepKeyed");
                Assert.assertEquals(Integer.valueOf(45), ((SimpleCMObject) object).getInteger("innerKeyToNumber"));
            }
        }));
        waitThenAssertTestResults();

        service.asyncLoadObject("oneKey", testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                Assert.assertTrue(response.wasSuccess());
                Assert.assertEquals(1, response.getObjects().size());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testAsyncSearch() {
        service.insert(COMPLEX_JSON);

        service.asyncSearch("[innerKey=\"inner spaced String\"]", testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                Assert.assertTrue(response.wasSuccess());
                Assert.assertEquals(1, response.getObjects().size());
                SimpleCMObject object = (SimpleCMObject) response.getCMObject("deepKeyed");

                SimpleCMObject anotherObject = object.getSimpleCMObject("anotherObject");

                Assert.assertEquals(Arrays.asList(1, 2, 3, 4, 5), anotherObject.getList("innerObjectKey"));
            }
        }));

        waitThenAssertTestResults();
    }

    @Test
    public void testAsyncGeoSearch() {
        SimpleCMObject geoObject = new SimpleCMObject("location");
        geoObject.add("geoPoint", new CMGeoPoint(50, 50));

        service.insert(geoObject.transportableRepresentation());

        service.asyncSearch("[geoPoint near (50, 50)]", testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                List<SimpleCMObject> objects = response.getObjects(SimpleCMObject.class);
                Assert.assertEquals(1, objects.size());

                CMGeoPointInterface geoPoint = objects.get(0).getGeoPoint("geoPoint");
                Assert.assertEquals(50, geoPoint.getLatitude(), 2);
                Assert.assertEquals(50, geoPoint.getLongitude(), 2);

            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testAsyncInsert() {
        SimpleCMObject deepObject = new SimpleCMObject(JsonUtilities.jsonCollection(DEEP_KEYED_JSON));
        SimpleCMObject simpleObject = new SimpleCMObject(JsonUtilities.jsonCollection(SIMPLE_JSON));
        service.asyncInsert(Arrays.asList(deepObject, simpleObject), testCallback(new ObjectModificationResponseCallback() {
            public void onCompletion(ObjectModificationResponse response) {
                Assert.assertTrue(response.wasSuccess());
                Assert.assertTrue(response.wasCreated("deepKeyed"));
                Assert.assertTrue(response.wasCreated("oneKey"));
            }
        }));
        waitThenAssertTestResults();

        deepObject.remove("innerKey");

        service.asyncInsert(Arrays.asList(deepObject, simpleObject), testCallback(new ObjectModificationResponseCallback() {
            public void onCompletion(ObjectModificationResponse response) {
                Assert.assertTrue(response.wasSuccess());
                Assert.assertTrue(response.wasUpdated("deepKeyed"));
                Assert.assertTrue(response.wasUpdated("oneKey"));
                CMObjectResponse loadObjectResponse = service.loadAllObjects();
                Assert.assertEquals(2, loadObjectResponse.getObjects().size());

                SimpleCMObject deepObject = (SimpleCMObject) loadObjectResponse.getCMObject("deepKeyed");
                Assert.assertNull(deepObject.get("innerKey"));
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testAsyncDelete() {
        service.insert(COMPLEX_JSON);

        service.asyncDelete("oneKey", testCallback(new ObjectModificationResponseCallback() {
            public void onCompletion(ObjectModificationResponse response) {
                List<SimpleCMObject> successObjects = response.getSuccessObjects();
                assertTrue(response.wasSuccess());
                assertTrue(response.wasDeleted("oneKey"));
                assertEquals(1, successObjects.size());
            }
        }));
        waitThenAssertTestResults();
        service.asyncDelete(Arrays.asList("deepKeyed", "oneKey"), testCallback(new ObjectModificationResponseCallback() {
            public void onCompletion(ObjectModificationResponse response) {
                List<SimpleCMObject> successObjects = response.getSuccessObjects();
                assertTrue(response.wasSuccess());
                assertTrue(response.wasDeleted("deepKeyed"));
                assertEquals(1, successObjects.size());
                assertEquals(1, response.getErrorObjects().size());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testAsyncUpload() throws IOException {
        InputStream input = getObjectInputStream();
        CMFile file = new CMFile(input);
        service.asyncUpload(file, testCallback(new FileCreationResponseCallback() {
            public void onCompletion(FileCreationResponse file) {
                assertTrue(file.wasSuccess());
                assertNotNull(file.getfileId());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testAsyncLoadFile() throws Exception {
        InputStream input = getObjectInputStream();
        final CMFile file = new CMFile(input, "fileKey", null);
        ResponseBase response = service.insert(file);
        assertTrue(response.wasSuccess());

        service.asyncLoadFile("fileKey", testCallback(new FileLoadCallback("fileKey") {

            public void onCompletion(FileLoadResponse loadedFileResponse) {
                assertTrue(loadedFileResponse.wasSuccess());
                CMFile loadedFile = loadedFileResponse.getFile();
                assertEquals(file, loadedFile);

            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testUrlCharacters() {
        service.asyncDelete("a key with spaces");
    }

    @Test
    public void testAsyncLoadNullFile() {
        service.asyncLoadFile("nonexistentfile", testCallback(new FileLoadCallback("nonexistent key for a file") {
            public void onCompletion(FileLoadResponse loadedFileResponse) {
                assertFalse(loadedFileResponse.wasSuccess());
                CMFile loadedFile = loadedFileResponse.getFile();
                assertEquals(1, loadedFile.getFileContents().length);
                assertTrue(CMFile.isEmpty(loadedFile));
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testAsyncDeleteFile() throws Exception {
        InputStream input = getObjectInputStream();
        final CMFile file = new CMFile(input, "fileKey", null);
        service.insert(file);

        service.asyncDeleteFile(file, testCallback(new ObjectModificationResponseCallback() {
            public void onCompletion(ObjectModificationResponse response) {
                assertTrue(response.wasSuccess());
                assertTrue(response.wasDeleted("fileKey"));
            }
        }));
        waitThenAssertTestResults();

    }

    @Test
    public void testAsyncUpdate() {
        //These tests are the same as above EXCEPT when innerKey is deleted and then inserted, the value
        //should still exist on the returned object. copy/paste code DELETE:
        SimpleCMObject deepObject = new SimpleCMObject(JsonUtilities.jsonCollection(DEEP_KEYED_JSON));
        SimpleCMObject simpleObject = new SimpleCMObject(JsonUtilities.jsonCollection(SIMPLE_JSON));
        service.asyncUpdate(Arrays.asList(deepObject, simpleObject),
                testCallback(new ObjectModificationResponseCallback() {
                    public void onCompletion(ObjectModificationResponse response) {
                        Assert.assertTrue(response.wasSuccess());
                        Assert.assertTrue(response.wasModified("deepKeyed"));
                        Assert.assertTrue(response.wasModified("oneKey")); //if these pass and below fail then delete between runs didn't happen
                        Assert.assertTrue(response.wasCreated("deepKeyed"));
                        Assert.assertTrue(response.wasCreated("oneKey"));
                    }
                }));
        waitThenAssertTestResults();

        deepObject.remove("innerKey");

        service.asyncUpdate(Arrays.asList(deepObject, simpleObject),
                testCallback(new ObjectModificationResponseCallback() {
                    public void onCompletion(ObjectModificationResponse response) {
                        Assert.assertTrue(response.wasSuccess());
                        Assert.assertTrue(response.wasUpdated("deepKeyed"));
                        Assert.assertTrue(response.wasUpdated("oneKey"));
                        CMObjectResponse loadObjectResponse = service.loadAllObjects();
                        Assert.assertEquals(2, loadObjectResponse.getObjects().size());

                        SimpleCMObject deepObject = (SimpleCMObject) loadObjectResponse.getCMObject("deepKeyed");
                        Assert.assertNotNull(deepObject.get("innerKey")); //This is where this test differs from above
                    }
                }));
        waitThenAssertTestResults();
    }


    private void assertWasSuccess(SuccessErrorResponse response) {
        Assert.assertNotNull(response);
        Assert.assertFalse(response.hasError());
        Assert.assertTrue(response.hasSuccess());
    }
}

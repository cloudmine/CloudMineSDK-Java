package com.cloudmine.api.rest;

import com.cloudmine.api.*;
import com.cloudmine.api.rest.callbacks.CloudMineResponseCallback;
import com.cloudmine.api.rest.callbacks.WebServiceCallback;
import org.apache.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/16/12, 2:40 PM
 */
public class CloudMineWebServiceIntegrationTest {

    private static final String TEST_JSON = "{\"TESTING4703\": [\"value1\", \"value2\"]}";

    private static final String COMPLEX_JSON = "{\n" +
            "    \"oneKey\":42,\n" +
            "    \"deepKeyed\": {\n" +
            "        \"innerKey\":\"inner spaced String\",\n" +
            "        \"innerKeyToNumber\":45,\n" +
            "        \"anotherObject\": {\n" +
            "            \"innerObjectKey\":[1, 2, 3, 4, 5]\n" +
            "        }\n" +
            "    }\n" +
            "}";
    public static final User USER = new User("francis@gmail.com", "GOD");
    private CloudMineWebService store;
    @Before
    public void setUp() {
        store = new CloudMineWebService(
                new CloudMineURLBuilder(
                        ApiCredentials.applicationIdentifier()),
                null);
    }
    @After
    public void cleanUp() {
        store.deleteAll();
    }

    @Test
    @Ignore
    public void testAsyncCreateUser() throws Exception {
        User newUser = new User("test2@test.com", "password");
        Future<CloudMineResponse> futureResponse = store.asyncCreateUser(newUser, new CloudMineResponseCallback() {
            @Override
            public void onCompletion(CloudMineResponse response) {
                assertTrue(response.was(201));
            }

            @Override
            public void onFailure(Throwable ex, String content) {
                ex.printStackTrace();
                fail("Failed");
            }
        });
        CloudMineResponse response = futureResponse.get(5, TimeUnit.SECONDS);
        assertTrue(response.was(201));
    }

    @Test
    public void testBasicSet() {
        CloudMineResponse response = store.set(TEST_JSON);
        assertWasSuccess(response);

        assertTrue(response.hasSuccessKey("TESTING4703"));
    }

    @Test
    public void testStringSearch() {
        store.set(COMPLEX_JSON);

        CloudMineResponse response = store.search("[innerKey=\"inner spaced String\"]");
        assertWasSuccess(response);
    }

    @Test
    public void testDeleteAll() {
        store.set(TEST_JSON);
        CloudMineResponse response = store.deleteAll();
        assertWasSuccess(response);
    }

    @Test
    public void testBasicUpdate() {
        CloudMineResponse response = store.update(TEST_JSON);
        assertWasSuccess(response);

        assertTrue(response.hasSuccessKey("TESTING4703"));
    }

    @Test
    public void testBasicGet() {
        store.set(TEST_JSON);
        CloudMineResponse response = store.get();
        assertWasSuccess(response);
        assertTrue(response.hasSuccessKey("TESTING4703"));
    }

    @Test
    public void testFileStorageSet() throws Exception {
        InputStream input = getObjectInputStream();
        CloudMineResponse response = store.set(new CloudMineFile(input));
        assertNotNull(response);
        assertFalse(response.hasError());
        assertTrue(response.hasNode("key"));
    }

    @Test
    public void testFileStorageGet() throws Exception {
        CloudMineFile insertedFile = new CloudMineFile(getObjectInputStream(), CloudMineFile.DEFAULT_CONTENT_TYPE, "theFileKey");
        CloudMineResponse response = store.set(
                insertedFile);

        CloudMineFile loadedFile = store.getObject("theFileKey");
        assertArrayEquals(insertedFile.getFileContents(), loadedFile.getFileContents());
    }

    @Test
    public void testAsyncObjectLoad() throws Exception {
        final SimpleCMObject task = new SimpleCMObject();
        task.setClass("task");
        task.add("name", "Do dishes");
        task.add("isDone", false);

        Future<CloudMineResponse> futureResponse =
                store.create(task, new WebServiceCallback() {
            @Override
            public void onCompletion(HttpResponse response)  {
                Future<SimpleObjectResponse> responseFuture = store.allObjectsOfClass("task");
                try {
                    SimpleObjectResponse objectResponse = responseFuture.get(5L, TimeUnit.SECONDS);
                    assertEquals(1, objectResponse.objects().size());
                    assertEquals(task, objectResponse.objects().get(0));
                } catch (Exception e) {
                    e.printStackTrace();
                    fail();
                }
            }

            @Override
            public void onFailure(Throwable error, String message) {
                error.printStackTrace();
                fail("failed! " + message);
            }
        });
        assertWasSuccess(futureResponse.get(5L, TimeUnit.SECONDS));
    }

    @Test
    public void testKeyedDelete() {
        store.set(COMPLEX_JSON);

        CloudMineResponse response = store.delete("deepKeyed");

        assertWasSuccess(response);

        response = store.get();
        assertWasSuccess(response);
        assertTrue(response.hasSuccessKey("oneKey"));
        assertFalse(response.hasSuccessKey("deepKeyed"));

        store.set(COMPLEX_JSON);

        store.delete("deepKeyed", "oneKey");
        response = store.get();
        assertFalse(response.hasSuccessKey("oneKey"));
        assertFalse(response.hasSuccessKey("deepKeyed"));
    }

    @Test
    @Ignore // until we can delete users this test will fail every time but the first time its run
    public void testCreateUser() {
        CloudMineResponse response = store.set(USER);
        assertTrue(response.was(201));
    }

    @Test
    public void testUserLogin() {
        User nonExistentUser = new User("some@dude.com", "123");
        LoginResponse response = store.login(nonExistentUser);
        assertTrue(response.was(401));
        store.set(USER);

        response = store.login(USER);
        assertTrue(response.was(200));
    }

    @Test
    public void testUserLogout() {
        CloudMineResponse response = store.logout(new UserToken("this token doesn't exist", new Date()));
        assertEquals(401, response.getStatusCode());

        store.set(USER);
        LoginResponse loginResponse = store.login(USER);
        assertTrue(loginResponse.was(200));

        response = store.logout(loginResponse.userToken());
        assertTrue(response.was(200));
    }

    private InputStream getObjectInputStream() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ObjectOutputStream objectOutput = new ObjectOutputStream(output);
        objectOutput.write(55);
        objectOutput.writeObject("Some String is Written");
        objectOutput.flush();
        objectOutput.close();

        return new ByteArrayInputStream(output.toByteArray());
    }

    private void assertWasSuccess(CloudMineResponse response) {
        assertNotNull(response);
        assertFalse(response.hasError());
        assertTrue(response.hasSuccess());

    }

}

package com.cloudmine.api.rest;

import com.cloudmine.api.ApiCredentials;
import com.cloudmine.api.CloudMineFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/16/12, 2:40 PM
 */
public class CloudMineStoreIntegrationTest {

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
    private CloudMineStore store;
    @Before
    public void setUp() {
        store = new CloudMineStore(new CloudMineURLBuilder(ApiCredentials.applicationIdentifier()));
    }
    @After
    public void cleanUp() {
//        store.deleteAll();
    }

    @Test
    public void testBasicSet() {
        CloudMineResponse response = store.set(TEST_JSON);
        assertWasSuccess(response);

        assertTrue(response.successHasKey("TESTING4703"));
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

        assertTrue(response.successHasKey("TESTING4703"));
    }

    @Test
    public void testBasicGet() {
        store.set(TEST_JSON);
        CloudMineResponse response = store.get();
        assertWasSuccess(response);
        assertTrue(response.successHasKey("TESTING4703"));
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
    public void testKeyedDelete() {
        store.set(COMPLEX_JSON);

        CloudMineResponse response = store.delete("deepKeyed");

        assertWasSuccess(response);

        response = store.get();
        assertWasSuccess(response);
        assertTrue(response.successHasKey("oneKey"));
        assertFalse(response.successHasKey("deepKeyed"));

        store.set(COMPLEX_JSON);

        store.delete("deepKeyed", "oneKey");
        response = store.get();
        assertFalse(response.successHasKey("oneKey"));
        assertFalse(response.successHasKey("deepKeyed"));
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

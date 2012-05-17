package com.cloudmine.api.rest;

import com.cloudmine.api.ApiCredentials;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/16/12, 2:40 PM
 */
public class CloudMineStoreIntegrationTest {

    public static final String TEST_JSON = "{\"TESTING4703\": [\"value1\", \"value2\"]}";
    private CloudMineStore store;

    @Before
    public void setUp() {
        store = getStore();
    }

    @After
    public void cleanUp() {
        getStore().deleteAll();
    }

    @Test
    public void testBasicSet() {
        CloudMineResponse response = store.set(TEST_JSON);
        assertWasSuccess(response);

        assertTrue(response.successHasKey("TESTING4703"));

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

    private void assertWasSuccess(CloudMineResponse response) {
        assertNotNull(response);
        assertFalse(response.hasError());
        assertTrue(response.hasSuccess());

    }

    private CloudMineStore getStore() {
        return new CloudMineStore(new CloudMineURLBuilder(ApiCredentials.applicationIdentifier()));
    }

}

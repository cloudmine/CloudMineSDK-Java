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
    public void testBasicPut() {
        CloudMineResponse response = createTestValue();
        assertNotNull(response);
        assertFalse(response.hasError());

        assertTrue(response.successHasKey("TESTING4703"));

    }

    @Test
    public void testDeleteAll() {
        createTestValue();
        CloudMineResponse response = store.deleteAll();
        assertNotNull(response);
        assertFalse(response.hasError());
        assertTrue(response.hasSuccess());
    }

    private CloudMineResponse createTestValue() {
        String json = "{\"TESTING4703\": [\"value1\", \"value2\"]}";
        return store.put(json);
    }

    @Test
    public void testBasicGet() {
        createTestValue();
        CloudMineResponse response = store.get();

        assertNotNull(response);
        assertFalse(response.hasError());
        assertTrue(response.successHasKey("TESTING4703"));
    }

    private CloudMineStore getStore() {
        return new CloudMineStore(new CloudMineURLBuilder(ApiCredentials.applicationIdentifier()));
    }

}

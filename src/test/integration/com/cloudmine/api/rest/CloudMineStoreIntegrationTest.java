package com.cloudmine.api.rest;

import com.cloudmine.api.ApiCredentials;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/16/12, 2:40 PM
 */
public class CloudMineStoreIntegrationTest {

    @Test
    public void testBasicPut() {
        CloudMineStore store = getStore();
        String json = "{\"key\": [\"value1\", \"value2\"]}";
        CloudMineResponse response = store.put(json);
        assertNotNull(response);
        assertFalse(response.hasError());

        assertTrue(response.successHasKey("key"));

    }

    @Test
    public void testBasicGet() {
        CloudMineStore store = getStore();
        //TODO Integration tests might need some setting up so they always run on a clean database - VM or something that can be spun up in a specific state?
        CloudMineResponse response = store.get();

        assertNotNull(response);
        assertFalse(response.hasError());
    }

    private CloudMineStore getStore() {
        return new CloudMineStore(new CloudMineURLBuilder("http://localhost:3001", ApiCredentials.applicationIdentifier()));
    }

}

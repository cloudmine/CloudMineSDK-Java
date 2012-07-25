package com.cloudmine.api.rest;

import com.cloudmine.api.rest.callbacks.CMResponseCallback;
import com.cloudmine.api.rest.callbacks.Callback;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class ResponseTimeDataStoreTest {

    @Test
    public void testGetContentAsString() {
        String content = ResponseTimeDataStore.getContentsAsStringAndClearMap();
        assertEquals("", content);
//this test kind of sucks but its going to be tested more in depth in an integration test
    }

    private Callback fakeCallback() {
        CMResponseCallback cmResponseCallback = new CMResponseCallback();
        cmResponseCallback.setStartTime(System.currentTimeMillis());
        return cmResponseCallback;
    }

    private HttpResponse fakeResponse() {
        HttpResponse response = new BasicHttpResponse(null);
        response.setHeader(HeaderFactory.REQUEST_ID_KEY, UUID.randomUUID().toString());
        return response;
    }
}

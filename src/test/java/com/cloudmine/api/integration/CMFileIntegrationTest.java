package com.cloudmine.api.integration;

import com.cloudmine.api.CMFile;
import com.cloudmine.api.rest.CMFileMetaData;
import com.cloudmine.api.rest.CMStore;
import com.cloudmine.api.rest.callbacks.CMObjectResponseCallback;
import com.cloudmine.api.rest.callbacks.FileCreationResponseCallback;
import com.cloudmine.api.rest.response.CMObjectResponse;
import com.cloudmine.api.rest.response.FileCreationResponse;
import com.cloudmine.test.ServiceTestBase;
import org.junit.Ignore;
import org.junit.Test;

import static com.cloudmine.test.AsyncTestResultsCoordinator.waitThenAssertTestResults;
import static com.cloudmine.test.TestServiceCallback.testCallback;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMFileIntegrationTest extends ServiceTestBase {


    @Test
    @Ignore //this should be updated to not use the store, but no one uses file metadata anyway
    public void testLoadFileMetaData() {
        final CMFile file = new CMFile(getObjectInputStream(), "application/oop");
        file.save(testCallback(new FileCreationResponseCallback() {
            public void onCompletion(FileCreationResponse response) {
                assertTrue(response.wasSuccess());
            }
        }));
        waitThenAssertTestResults();
        assertEquals(0, CMStore.getStore().getStoredObjects().size());

        CMStore.getStore().loadApplicationFileMetaData(file.getFileId(),
                testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                assertTrue(response.wasSuccess());
                int returned = 0;
                for(CMFileMetaData data : response.getObjects(CMFileMetaData.class)) {
                    assertEquals("application/oop", data.getContentType());
                    assertEquals(file.getFileId(), data.getFilename());
                    returned++;
                }
                assertEquals(1, returned);
            }
        }));
        waitThenAssertTestResults();

        assertEquals(1, CMStore.getStore().getStoredObjects().size());
    }
}

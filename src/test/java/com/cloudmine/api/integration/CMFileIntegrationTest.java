package com.cloudmine.api.integration;

import com.cloudmine.api.CMFile;
import com.cloudmine.api.rest.callbacks.FileCreationResponseCallback;
import com.cloudmine.api.rest.response.FileCreationResponse;
import com.cloudmine.test.ServiceTestBase;
import org.junit.Test;

import static com.cloudmine.test.AsyncTestResultsCoordinator.waitThenAssertTestResults;
import static com.cloudmine.test.TestServiceCallback.testCallback;
import static junit.framework.Assert.assertTrue;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMFileIntegrationTest extends ServiceTestBase {


    @Test
    public void testChangefileId() {
        CMFile file = new CMFile(getObjectInputStream(), "thefileId");
        file.save(testCallback(new FileCreationResponseCallback() {
            public void onCompletion(FileCreationResponse response) {
                assertTrue(response.wasSuccess());
            }
        }));

        waitThenAssertTestResults();
    }
}

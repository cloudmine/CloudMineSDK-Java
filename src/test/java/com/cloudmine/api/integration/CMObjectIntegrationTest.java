package com.cloudmine.api.integration;

import com.cloudmine.api.CMGeoPoint2;
import com.cloudmine.api.rest.CMStore;
import com.cloudmine.api.rest.callbacks.CMObjectResponseCallback;
import com.cloudmine.api.rest.response.CMObjectResponse;
import com.cloudmine.test.ExtendedCMObject;
import com.cloudmine.test.GeoCMObject;
import com.cloudmine.test.ServiceTestBase;
import org.junit.Test;

import static com.cloudmine.test.AsyncTestResultsCoordinator.waitThenAssertTestResults;
import static com.cloudmine.test.TestServiceCallback.testCallback;
import static org.junit.Assert.assertEquals;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */

public class CMObjectIntegrationTest extends ServiceTestBase{

    @Test
    public void testSave() {
        ExtendedCMObject object = new ExtendedCMObject();
        object.save(hasSuccessAndHasModified(object));
        waitThenAssertTestResults();

        service.asyncLoadObject(object.getObjectId(), hasSuccessAndHasLoaded(object));
        waitThenAssertTestResults();
    }
}

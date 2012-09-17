package com.cloudmine.api.integration;

import com.cloudmine.api.*;
import com.cloudmine.api.exceptions.CreationException;
import com.cloudmine.api.rest.CMStore;
import com.cloudmine.api.rest.CMWebService;
import com.cloudmine.api.rest.callbacks.CMObjectResponseCallback;
import com.cloudmine.api.rest.callbacks.ObjectModificationResponseCallback;
import com.cloudmine.api.rest.response.CMObjectResponse;
import com.cloudmine.api.rest.response.ObjectModificationResponse;
import com.cloudmine.test.ServiceTestBase;
import junit.framework.Assert;
import org.junit.Test;

import static com.cloudmine.test.AsyncTestResultsCoordinator.waitThenAssertTestResults;
import static com.cloudmine.test.TestServiceCallback.testCallback;
import static junit.framework.Assert.*;

/**
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 * User: johnmccarthy
 * Date: 6/14/12, 10:53 AM
 */

public class SimpleCMObjectIntegrationTest extends ServiceTestBase {

    @Test
    public void testDefaultSave() {
        final SimpleCMObject object = new SimpleCMObject();
        object.add("string", "value");
        object.save(testCallback(new ObjectModificationResponseCallback() {
            public void onCompletion(ObjectModificationResponse response) {
                CMObjectResponse loadResponse = CMWebService.getService().loadObject(object.getObjectId());
                Assert.assertTrue(loadResponse.wasSuccess());
                SimpleCMObject loadedObject = (SimpleCMObject) loadResponse.getCMObject(object.getObjectId());
                assertEquals(object, loadedObject);
            }
        }));
        waitThenAssertTestResults();
        CMUser user = user();
        service.insert(user);
        CMSessionToken token = service.login(user).getSessionToken();
        assertFalse(object.setSaveWith(user));

        object.save();
        SimpleCMObject loadedObject = (SimpleCMObject)service.getUserWebService(token).loadObject(object.getObjectId()).getCMObject(object.getObjectId());
        assertNull(loadedObject);

    }

    @Test
    public void testUserSave() {
        final SimpleCMObject object = new SimpleCMObject();
        object.add("bool", true);
        final CMUser user = user();
        object.setSaveWith(user);
        object.save(testCallback(new ObjectModificationResponseCallback() {
            public void onCompletion(ObjectModificationResponse response) {
                CMObjectResponse loadedObjectResponse = service.loadObject(object.getObjectId());
                SimpleCMObject loadedObject = (SimpleCMObject) loadedObjectResponse.getCMObject(object.getObjectId());

                Assert.assertNull(loadedObject);
            }

        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testSaveWithUser() {
        final SimpleCMObject object = simpleObject();
        object.saveWithUser(user(), hasSuccessAndHasModified(object));
        waitThenAssertTestResults();

        SimpleCMObject appObject = simpleObject();
        appObject.setSaveWith(StoreIdentifier.applicationLevel());
        try {
            appObject.saveWithUser(user());
            fail();
        } catch(CreationException ce) {
            //expected
        }
    }


    @Test
    public void testSimpleGeoPoint() {
        SimpleCMObject object = new SimpleCMObject();
        object.add("test", new CMGeoPoint(55, 55));
        object.save(hasSuccess);
        waitThenAssertTestResults();

        CMStore.getStore().loadApplicationObjectWithObjectId(object.getObjectId(), testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                SimpleCMObject loaded = (SimpleCMObject)response.getObjects().get(0);
                assertEquals(55.0, loaded.getGeoPoint("test"));
            }
        }));
        waitThenAssertTestResults();
    }
}

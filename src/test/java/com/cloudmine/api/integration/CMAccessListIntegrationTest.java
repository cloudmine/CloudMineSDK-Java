package com.cloudmine.api.integration;

import com.cloudmine.api.*;
import com.cloudmine.api.rest.UserCMWebService;
import com.cloudmine.api.rest.callbacks.CMObjectResponseCallback;
import com.cloudmine.api.rest.callbacks.CreationResponseCallback;
import com.cloudmine.api.rest.options.CMRequestOptions;
import com.cloudmine.api.rest.options.CMSharedDataOptions;
import com.cloudmine.api.rest.response.CMObjectResponse;
import com.cloudmine.api.rest.response.CreationResponse;
import com.cloudmine.test.ServiceTestBase;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.cloudmine.test.AsyncTestResultsCoordinator.waitThenAssertTestResults;
import static com.cloudmine.test.TestServiceCallback.testCallback;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMAccessListIntegrationTest extends ServiceTestBase {

    @Test
    public void testStoreAccessList() {
        final JavaCMUser anotherUser = createOtherUser();

        final JavaCMUser user = createMainUser();

        JavaAccessListController list = getCmAccessList(anotherUser, user);
        list.save(testCallback(new CreationResponseCallback() {
            @Override
            public void onCompletion(CreationResponse response) {
                assertTrue(response.wasSuccess());
//                assertEquals(list.getObjectId(), response.getObjectId());
            }
        }));
        waitThenAssertTestResults();


        final SimpleCMObject anObject = insertAnObject(user, list);

        anotherUser.login(hasSuccess);
        waitThenAssertTestResults();
        CMSessionToken token = anotherUser.getSessionToken();

        UserCMWebService userWebService = service.getUserWebService(token);
        userWebService.asyncLoadObject(anObject.getObjectId(), testCallback(new CMObjectResponseCallback() {
            @Override
            public void onCompletion(CMObjectResponse response) {
                assertTrue(response.hasSuccess());
                assertEquals(1, response.getObjects().size());
                CMObject loadedObject = response.getCMObject(anObject.getObjectId());
                assertEquals(anObject, loadedObject);
            }
        }), CMRequestOptions.CMRequestOptions(CMSharedDataOptions.SHARED_OPTIONS));
        waitThenAssertTestResults();

        CMRequestOptions requestOptions = new CMRequestOptions(CMSharedDataOptions.getShared());
        userWebService.asyncLoadObjects(testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {

                assertTrue(response.hasSuccess());
                assertEquals(1, response.getObjects().size());
                CMObject loadedObject = response.getCMObject(anObject.getObjectId());
                assertEquals(anObject, loadedObject);
            }

        }), requestOptions);
        waitThenAssertTestResults();
    }

    protected SimpleCMObject insertAnObject(JavaCMUser user, JavaAccessListController list) {
        final SimpleCMObject anObject = new SimpleCMObject();
        anObject.add("aSecret", true);
        anObject.grantAccess(list);
        anObject.saveWithUser(user, hasSuccessAndHasModified(anObject));
        waitThenAssertTestResults();
        return anObject;
    }

    protected JavaAccessListController getCmAccessList(JavaCMUser anotherUser, JavaCMUser user) {
        List<String> userObjectIds = Arrays.asList("freddy", "teddy", "george", "puddin");
        JavaAccessListController list = new JavaAccessListController(user, CMAccessPermission.READ, CMAccessPermission.UPDATE);
        list.grantAccessTo(userObjectIds);
        list.grantAccessTo(anotherUser);
        list.grantPermissions(CMAccessPermission.READ);
        return list;
    }

    protected JavaCMUser createMainUser() {
        final JavaCMUser user = user();
        user.createUser(testCallback());
        waitThenAssertTestResults();
        user.login(hasSuccess);
        waitThenAssertTestResults();
        return user;
    }

    protected JavaCMUser createOtherUser() {
        final JavaCMUser anotherUser = randomUser();
        anotherUser.createUser(hasSuccess);
        waitThenAssertTestResults();
        return anotherUser;
    }

    @Test
    public void testGetAccessList() {
        JavaCMUser user = user();
        final JavaAccessListController list = new JavaAccessListController(user, CMAccessPermission.CREATE);
        list.grantAccessTo("whatever");
        list.save(hasSuccess);
        waitThenAssertTestResults();

        user.loadAccessLists(testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                assertTrue(response.wasSuccess());
                CMObject loadedList = response.getCMObject(list.getObjectId());
                assertEquals(list, loadedList);
            }
        }));
        waitThenAssertTestResults();
    }
}

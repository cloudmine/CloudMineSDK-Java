package com.cloudmine.api.integration;

import com.cloudmine.api.CMObject;
import com.cloudmine.api.CMSessionToken;
import com.cloudmine.api.CMUser;
import com.cloudmine.api.rest.callbacks.*;
import com.cloudmine.api.rest.response.*;
import com.cloudmine.api.rest.response.code.CMResponseCode;
import com.cloudmine.test.ExtendedCMUser;
import com.cloudmine.test.ServiceTestBase;
import com.cloudmine.test.TestServiceCallback;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.cloudmine.test.AsyncTestResultsCoordinator.waitThenAssertTestResults;
import static com.cloudmine.test.TestServiceCallback.testCallback;
import static junit.framework.Assert.*;

/**
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */

public class CMUserIntegrationTest extends ServiceTestBase {

    @Test
    public void testLogin() throws ExecutionException, TimeoutException, InterruptedException {
        final CMUser user = new CMUser("test13131313@test.com", "test");
        service.insert(user);

        user.login(testCallback(new LoginResponseCallback() {
            public void onCompletion(LoginResponse loginResponse) {
                CMSessionToken token = loginResponse.getSessionToken();
                assertTrue(loginResponse.wasSuccess());
                assertTrue(user.isLoggedIn());
                assertFalse(CMSessionToken.INVALID_TOKEN.equals(token));
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testLogout() {
        CMUser user = user();

        user.login(hasSuccess);
        waitThenAssertTestResults();

        user.logout(hasSuccess);
        waitThenAssertTestResults();
        assertFalse(user.isLoggedIn());
    }

    @Test
    public void testChangePassword() {
        CMUser user = new CMUser(randomEmail(), "test");
        user.createUser(testCallback());
        waitThenAssertTestResults();
        user.changePassword("12345", testCallback(new CMResponseCallback() {
            public void onCompletion(CMResponse response) {
                assertTrue(response.wasSuccess());
            }
        }));
        waitThenAssertTestResults();
        assertEquals("12345", user.getPassword());
        user.login(testCallback(new LoginResponseCallback() {
            public void onCompletion(LoginResponse response) {
                assertTrue(response.wasSuccess());
            }
        }));
        waitThenAssertTestResults();

        user.changePassword("12345", "test", testCallback(new CMResponseCallback() {
            public void onCompletion(CMResponse response) {
                assertTrue(response.wasSuccess());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testCreateUser() {
        final CMUser user = new CMUser(randomEmail(), "w");

        user.createUser(TestServiceCallback.testCallback(new CreationResponseCallback() {
            @Override
            public void onCompletion(CreationResponse response) {
                assertTrue(response.wasSuccess());
                assertEquals(response.getObjectId(), user.getObjectId());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testCreateUserErrors() {
        CMUser user = new CMUser("@.notanEm!l", "pw");
        user.createUser(TestServiceCallback.testCallback(new CreationResponseCallback() {
            @Override
            public void onCompletion(CreationResponse response) {
                assertEquals(CMResponseCode.INVALID_EMAIL_OR_MISSING_PASSWORD, response.getResponseCode());
            }
        }));
        waitThenAssertTestResults();
        user = new CMUser("vali@email.com", "");
        user.createUser(TestServiceCallback.testCallback(new CreationResponseCallback() {
            @Override
            public void onCompletion(CreationResponse response) {
                assertEquals(CMResponseCode.INVALID_EMAIL_OR_MISSING_PASSWORD, response.getResponseCode());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testUserProfile() {
        ExtendedCMUser user = new ExtendedCMUser("frexxd@francis.com", "pw");
        user.save(testCallback());
        waitThenAssertTestResults();
        user.login(hasSuccess);
        waitThenAssertTestResults();
        user.setAge(50);
        user.save(hasSuccess);
        waitThenAssertTestResults();

        ExtendedCMUser reloadedUser = new ExtendedCMUser("frexxd@francis.com", "pw");
        reloadedUser.login(hasSuccess);
        waitThenAssertTestResults();
        assertEquals(50, reloadedUser.getAge());
    }

    @Test
    public void testSearchUserProfiles() {
        final ExtendedCMUser user = new ExtendedCMUser("ewoirusldfjlsjreoijrwlejfldjfsljfoweirudlsj.vc.ncosrjf@gmail.com", "12345");
        user.setAge(500);
        user.createUser(testCallback());
        waitThenAssertTestResults();
        user.login(hasSuccess);
        waitThenAssertTestResults();
        CMUser.searchUserProfiles("[age>400]", testCallback(new CMObjectResponseCallback() {
            public void onCompletion(CMObjectResponse response) {
                for(CMObject object : response.getObjects()) {
                    if(object instanceof ExtendedCMUser) {
                        assertEquals(user, object);
                    }
                }
            }
        }));
        waitThenAssertTestResults();
    }
}

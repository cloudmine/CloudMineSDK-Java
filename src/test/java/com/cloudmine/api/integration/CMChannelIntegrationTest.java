package com.cloudmine.api.integration;

import com.cloudmine.api.CMApiCredentials;
import com.cloudmine.api.CMChannel;
import com.cloudmine.api.CMPushNotification;
import com.cloudmine.api.JavaCMUser;
import com.cloudmine.api.rest.CMWebService;
import com.cloudmine.api.rest.callbacks.CMResponseCallback;
import com.cloudmine.api.rest.callbacks.ListOfStringsCallback;
import com.cloudmine.api.rest.callbacks.PushChannelResponseCallback;
import com.cloudmine.api.rest.response.CMResponse;
import com.cloudmine.api.rest.response.ListOfValuesResponse;
import com.cloudmine.api.rest.response.PushChannelResponse;
import com.cloudmine.test.ServiceTestBase;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.cloudmine.test.AsyncTestResultsCoordinator.reset;
import static com.cloudmine.test.AsyncTestResultsCoordinator.waitThenAssertTestResults;
import static com.cloudmine.test.TestServiceCallback.testCallback;
import static junit.framework.Assert.*;


/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMChannelIntegrationTest extends ServiceTestBase {

    public static final String APP_ID = "c1a562ee1e6f4a478803e7b51babe287";
    public static final String API_KEY = "27D924936D2C7D422D58B919B9F23653";
    private static final String CHANNEL_NAME = "cloudmine";

    public void setUp() {
        super.setUp();
        CMApiCredentials.initialize(APP_ID, API_KEY);
        service = CMWebService.getService(APP_ID, API_KEY);
    }

    @Test
    public void testCreateChannel() {
        final String channelName = randomString();
        CMChannel channel = new CMChannel(channelName,
                Collections.<String>emptyList(),
                Collections.<String>emptyList());
        CMWebService.getService().asyncCreateChannel(channel, testCallback(new PushChannelResponseCallback() {
            public void onCompletion(PushChannelResponse response) {
                assertTrue(response.wasSuccess());
                assertEquals(channelName, response.getChannelName());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testChannelPush() {
        CMPushNotification notification = new CMPushNotification();
        notification.setChannel(CHANNEL_NAME);
        notification.addMessageRecipient(new CMPushNotification.UserIdTarget("abcdef12345"));
        notification.addMessageRecipient(new CMPushNotification.UserNameTarget("Bill"));
        notification.addMessageRecipient(new CMPushNotification.DeviceTarget("aoeui"));
        notification.setMessage("and the tide was way out");

        notification.send(testCallback(new CMResponseCallback() {
            public void onCompletion(CMResponse response) {
                assertTrue(response.wasSuccess());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testDeleteChannel() {
        CMChannel channel = new CMChannel();
        String channelName = randomString();
        channel.setName(channelName);
        channel.create(hasSuccess);
        waitThenAssertTestResults();
        channel.delete(testCallback(new CMResponseCallback() {
            public void onCompletion(CMResponse cmResponse) {
                assertTrue(cmResponse.wasSuccess());
            }
        }));
        waitThenAssertTestResults();
    }


    @Test
    public void testSelfSubscribers() {
        CMChannel channel = new CMChannel();
        final String channelName = randomString();
        channel.setName(channelName);
        channel.create(hasSuccess);
        waitThenAssertTestResults();

        JavaCMUser user = user();
        user.subscribeToChannel(channelName, testCallback(new CMResponseCallback() {
            public void onCompletion(CMResponse response){
                assertTrue(response.wasSuccess());
            }
        }));
        waitThenAssertTestResults();

        user.loadSubscribedChannels(testCallback(new ListOfStringsCallback() {
            public void onCompletion(ListOfValuesResponse <String> response){
                assertTrue(response.getValues().contains(channelName));
            }
        }));
        waitThenAssertTestResults();

        channel.delete(testCallback(new CMResponseCallback() {
            public void onCompletion(CMResponse cmResponse) {
                assertTrue(cmResponse.wasSuccess());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testLoadChannelInformation() {
        CMChannel channel = new CMChannel();
        final String channelName = randomString();
        channel.setName(channelName);
        channel.create(hasSuccess);
        waitThenAssertTestResults();

        reset(3);
        final JavaCMUser randomUser = randomUser();
        randomUser.createUser(hasSuccess);
        final JavaCMUser randomEmailUser = randomUser();
        randomEmailUser.createUser(hasSuccess);
        final JavaCMUser randomUsernameUser = JavaCMUser.CMUserWithUserName(randomString(), "test");
        randomUsernameUser.createUser(hasSuccess);
        waitThenAssertTestResults();

//        CMWebService.getService().asyncLoadChannelInformation(channelName, testCallback(new PushChannelResponseCallback() {
//            public void onCompletion(PushChannelResponse response) {
//                List<String> objectIds = response.getUserIds();
//                assertTrue(objectIds.contains(randomUser.getObjectId()));
//                assertTrue(objectIds.contains(randomEmailUser.getObjectId()));
//                assertTrue(objectIds.contains(randomUsernameUser.getObjectId()));
//            }
//        }));
//
//        waitThenAssertTestResults();

        channel.delete(testCallback(new CMResponseCallback() {
            public void onCompletion(CMResponse cmResponse) {
                assertTrue(cmResponse.wasSuccess());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testNonLoggedInSubscription() {
        CMChannel channel = new CMChannel();
        final String channelName = randomString();
        channel.setName(channelName);
        channel.create(hasSuccess);
        waitThenAssertTestResults();

        reset(3);
        final JavaCMUser randomUser = randomUser();
        randomUser.createUser(hasSuccess);
        final JavaCMUser randomEmailUser = randomUser();
        randomEmailUser.createUser(hasSuccess);
        final JavaCMUser randomUsernameUser = JavaCMUser.CMUserWithUserName(randomString(), "test");
        randomUsernameUser.createUser(hasSuccess);
        waitThenAssertTestResults();

        CMWebService.getService().asyncSubscribeUsersToChannel(channelName, Arrays.asList(
                new CMPushNotification.UserIdTarget(randomUser.getObjectId()),
                new CMPushNotification.EmailTarget(randomEmailUser.getEmail()),
                new CMPushNotification.UserNameTarget(randomUsernameUser.getUserName())),
                testCallback(new PushChannelResponseCallback() {
                    public void onCompletion(PushChannelResponse response) {
                        assertTrue(response.wasSuccess());
                        List<String> subscribedIds = response.getUserIds();
                        assertTrue(subscribedIds.contains(randomUser.getObjectId()));
                        assertTrue(subscribedIds.contains(randomEmailUser.getObjectId()));
                        assertTrue(subscribedIds.contains(randomUsernameUser.getObjectId()));
                    }
                })
                );
        waitThenAssertTestResults();

        assertUserHasChannel(channelName, randomUser);
        assertUserHasChannel(channelName, randomEmailUser);
        assertUserHasChannel(channelName, randomUsernameUser);

        channel.delete(testCallback(new CMResponseCallback() {
            public void onCompletion(CMResponse cmResponse) {
                assertTrue(cmResponse.wasSuccess());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testBulkUnsubscribe() {

        CMChannel channel = new CMChannel();
        final String channelName = randomString();
        channel.setName(channelName);
        channel.create(hasSuccess);
        waitThenAssertTestResults();

        reset(3);
        final JavaCMUser randomUser = randomUser();
        randomUser.createUser(hasSuccess);
        final JavaCMUser randomEmailUser = randomUser();
        randomEmailUser.createUser(hasSuccess);
        final JavaCMUser randomUsernameUser = JavaCMUser.CMUserWithUserName(randomString(), "test");
        randomUsernameUser.createUser(hasSuccess);
        waitThenAssertTestResults();

        CMWebService.getService().asyncSubscribeUsersToChannel(channelName, Arrays.asList(
                new CMPushNotification.UserIdTarget(randomUser.getObjectId()),
                new CMPushNotification.EmailTarget(randomEmailUser.getEmail()),
                new CMPushNotification.UserNameTarget(randomUsernameUser.getUserName())),
                hasSuccess);
        waitThenAssertTestResults();

        CMWebService.getService().asyncUnsubscribeUsersFromChannel(channelName, Arrays.asList(randomUser.getObjectId(), randomEmailUser.getObjectId(), randomUsernameUser.getObjectId()),
                testCallback(new PushChannelResponseCallback() {
                    public void onCompletion(PushChannelResponse response) {
                        assertTrue(response.wasSuccess());
                        List<String> subscribedUserIds = response.getUserIds();
                        assertEquals(0, subscribedUserIds.size());
                    }
                }));
        waitThenAssertTestResults();
        CMWebService.getService().asyncLoadSubscribedChannelsForUser(randomUser.getObjectId(), testCallback(new ListOfStringsCallback() {
            public void onCompletion(ListOfValuesResponse<String> response) {
                assertEquals(0, response.getValues().size());
            }
        }));
        waitThenAssertTestResults();

        channel.delete(testCallback(new CMResponseCallback() {
            public void onCompletion(CMResponse cmResponse) {
                assertTrue(cmResponse.wasSuccess());
            }
        }));
        waitThenAssertTestResults();
    }

    @Test
    public void testUnsubscribeSelf() {
        JavaCMUser user = user();

        user.subscribeToChannel(CHANNEL_NAME, hasSuccess);
        waitThenAssertTestResults();

        user.unsubscribeFromChannel(CHANNEL_NAME, hasSuccess);
        waitThenAssertTestResults();

        service.asyncLoadSubscribedChannelsForUser(user.getObjectId(), testCallback(new ListOfStringsCallback() {
            public void onCompletion(ListOfValuesResponse<String> response) {
                assertTrue(response.wasSuccess());
                List<String> responseStrings = response.getValues();

                assertFalse(responseStrings.contains(CHANNEL_NAME));
            }
        }));
        waitThenAssertTestResults();
    }

    private void assertUserHasChannel(final String channelName, JavaCMUser randomUser) {
        service.asyncLoadSubscribedChannelsForUser(randomUser.getObjectId(), testCallback(new ListOfStringsCallback() {
            public void onCompletion(ListOfValuesResponse<String> response) {
                assertTrue(response.wasSuccess());
                List<String> responseStrings = response.getValues();
                assertEquals(1, responseStrings.size());
                assertEquals(channelName, responseStrings.get(0));
            }
        }));
        waitThenAssertTestResults();
    }

}

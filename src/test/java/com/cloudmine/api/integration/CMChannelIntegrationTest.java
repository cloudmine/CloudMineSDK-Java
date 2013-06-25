package com.cloudmine.api.integration;

import com.cloudmine.api.CMChannel;
import com.cloudmine.api.CMPushNotification;
import com.cloudmine.api.CMUser;
import com.cloudmine.api.rest.CMWebService;
import com.cloudmine.api.rest.callbacks.CMResponseCallback;
import com.cloudmine.api.rest.callbacks.ListOfStringsCallback;
import com.cloudmine.api.rest.response.CMResponse;
import com.cloudmine.api.rest.response.ListOfValuesResponse;
import com.cloudmine.test.ServiceTestBase;
import org.junit.Test;

import java.util.Collections;

import static com.cloudmine.test.AsyncTestResultsCoordinator.waitThenAssertTestResults;
import static com.cloudmine.test.TestServiceCallback.testCallback;
import static junit.framework.Assert.assertTrue;


/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMChannelIntegrationTest extends ServiceTestBase {

    private static final String CHANNEL_NAME = "cloudmine";

    @Test
    public void testCreateChannel() {
        String channelName = randomString();
        CMChannel channel = new CMChannel(channelName, Collections.<String>emptyList(), Collections.<String>emptyList());
        CMWebService.getService().asyncCreateChannel(channel, testCallback(new CMResponseCallback() {
            public void onCompletion(CMResponse response) {
                assertTrue(response.wasSuccess());
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
    public void testSubscribers() {
        CMChannel channel = new CMChannel();
        final String channelName = randomString();
        channel.setName(channelName);
        channel.create(hasSuccess);
        waitThenAssertTestResults();

        CMUser user = user();
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
}

package com.cloudmine.api;

import com.cloudmine.api.rest.JsonUtilities;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: johnmccarthy
 * Date: 5/15/13
 * Time: 11:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class CMPushNotificationTest {

    @Test
    public void testSerializeDeserialize() {
        CMPushNotification notification = new CMPushNotification();
        notification.setMessage("Lets all go to the movies, lets all go to a show");
        notification.addMessageRecipient(new CMPushNotification.UserNameTarget("Bill"));
        notification.addMessageRecipient(new CMPushNotification.EmailTarget("fran@gmail.com"));
        notification.addMessageRecipient(new CMPushNotification.ChannelTarget("eggs"));

        String json = notification.transportableRepresentation();

        CMPushNotification recreated = JsonUtilities.jsonToClass(json, CMPushNotification.class);
        assertEquals(notification, recreated);
    }
}

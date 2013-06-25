package com.cloudmine.api;

    import com.cloudmine.api.rest.JsonUtilities;
    import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 */
public class CMPushNotificationTest {

    @Test
    public void testSerializeDeserialize() {
        CMPushNotification notification = new CMPushNotification();
        notification.setMessage("Lets all go to the movies, lets all go to a show");
        notification.addMessageRecipient(new CMPushNotification.EmailTarget("fran@gmail.com"));
        notification.addMessageRecipient(new CMPushNotification.UserIdTarget("abcdef12345"));
        notification.addMessageRecipient(new CMPushNotification.UserNameTarget("Bill"));
        notification.addMessageRecipient(new CMPushNotification.DeviceTarget("aoeui"));
        notification.setChannel("canada");

        String json = notification.transportableRepresentation();
        String expectedJson = "{\n" +
                "\"channel\":\"canada\",\n" +
                "\"users\":[{\"email\":\"fran@gmail.com\"}, {\"userid\":\"abcdef12345\"}, {\"username\":\"Bill\"}],\n" +
                "\"device_ids\":[\"aoeui\"],\n" +
                "\"text\":\"Lets all go to the movies, lets all go to a show\"" +
                "}";
        assertTrue(JsonUtilities.isJsonEquivalent(expectedJson, json));
    }
}

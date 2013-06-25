package com.cloudmine.api;

import com.cloudmine.api.exceptions.ConversionException;
import com.cloudmine.api.rest.CMWebService;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.Transportable;
import com.cloudmine.api.rest.callbacks.CMResponseCallback;
import com.cloudmine.api.rest.callbacks.Callback;
import com.cloudmine.api.rest.response.CMResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Model of a push notification. Notifications can be sent to a user's email, username, or to an entire channel
 */
public class CMPushNotification implements Transportable {


    public interface Target {
    }

    public static class UserIdTarget implements Target {
        private String userId;
        public UserIdTarget() {}
        public UserIdTarget(String userId) {
            this.userId = userId;
        }

        public String getUserId() {

            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UserIdTarget that = (UserIdTarget) o;

            if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return userId != null ? userId.hashCode() : 0;
        }
    }

    public static class DeviceTarget implements Target {
        private String deviceId;

        public DeviceTarget() {}
        public DeviceTarget(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DeviceTarget that = (DeviceTarget) o;

            if (deviceId != null ? !deviceId.equals(that.deviceId) : that.deviceId != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return deviceId != null ? deviceId.hashCode() : 0;
        }
    }

    public static class UserNameTarget implements Target {
        private String username;

        public UserNameTarget() {}

        public UserNameTarget(String username) {
            this.username = username;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UserNameTarget that = (UserNameTarget) o;

            if (username != null ? !username.equals(that.username) : that.username != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return username != null ? username.hashCode() : 0;
        }
    }

    public static class EmailTarget implements Target {
        private String email;

        public EmailTarget() {}

        public EmailTarget(String email) {
            this.email = email;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EmailTarget that = (EmailTarget) o;

            if (email != null ? !email.equals(that.email) : that.email != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return email != null ? email.hashCode() : 0;
        }
    }

    public static final String MESSAGE_KEY = "text";
    public static final String USERS_KEY = "users";


    @JsonProperty("users")
    private List<Target> messageRecipients;
    @JsonProperty("device_ids")
    private List<String> deviceTargets;
    @JsonProperty("channel")
    private String channel;

    @JsonProperty("text")
    private String message;

    public CMPushNotification() {
        this("", new ArrayList<Target>(), null);
    }

    public CMPushNotification(String message, List<Target> messageRecipients, String channelName) {
        this.message = message;
        extractTargetValues(messageRecipients);
        if(Strings.isNotEmpty(channelName)) channel = channelName;
    }

    private void extractTargetValues(List<Target> newMessageRecipients) {
        for(Target target : newMessageRecipients) {
            addTarget(target);
        }
    }

    private void addTarget(Target target) {
        if(target instanceof DeviceTarget) {
            if(deviceTargets == null) deviceTargets = new ArrayList<String>();
            deviceTargets.add(((CMPushNotification.DeviceTarget) target).getDeviceId());
        } else {
            if(messageRecipients == null) messageRecipients = new ArrayList<Target>();
            messageRecipients.add(target);
        }
    }

    public List<? extends Target> getMessageRecipients() {
        return messageRecipients;
    }

    public void setMessageRecipients(List<Target> messageRecipients) {
        extractTargetValues(messageRecipients);
    }

    public void addMessageRecipient(Target recipient) {
        addTarget(recipient);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getDeviceTargets() {
        return deviceTargets;
    }

    public void setDeviceTargets(List<String> deviceTargets) {
        this.deviceTargets = deviceTargets;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void send() {
        send(CMResponseCallback.<CMResponse>doNothing());
    }

    public void send(Callback<CMResponse> callback) {
        CMWebService.getService().asyncSendNotification(this, callback);
    }

    @Override
    public String transportableRepresentation() throws ConversionException {
        return JsonUtilities.objectToJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CMPushNotification that = (CMPushNotification) o;

        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        if (messageRecipients != null ? !messageRecipients.equals(that.messageRecipients) : that.messageRecipients != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = messageRecipients != null ? messageRecipients.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }
}

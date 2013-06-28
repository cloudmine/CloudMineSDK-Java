package com.cloudmine.api;

import com.cloudmine.api.exceptions.ConversionException;
import com.cloudmine.api.rest.CMWebService;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.Transportable;
import com.cloudmine.api.rest.callbacks.CMCallback;
import com.cloudmine.api.rest.response.CMResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * A channel for sending push notifications too. Defines specific users or devices to receive pushes. Instances are only used for creating
 * channels; modifications to a channel are done through static methods
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class CMChannel implements Transportable {

    /**
     * Delete the channel with the specified name. Note that a success response will be returned even if the channel name
     * given doesn't match any existing channels.
     * @param channelName
     * @param callback a CMResponseCallback
     */
    public static void delete(String channelName, CMCallback<CMResponse> callback) {
        CMWebService.getService().asyncDeleteChannel(channelName, callback);
    }

    private String name;
    private List<String> users;
    private List<String> deviceIds;

    public CMChannel() {}

    /**
     * A channel with no users or devices associated with it
     * @param name the name of the channel
     */
    public CMChannel(String name) {
        this(name, new ArrayList<String>(), new ArrayList<String>());
    }

    /**
     *
     * @param name The name of the channel
     * @param users the userids to be added to the channel.
     * @param deviceIds the device ids to be added to the channel.
     */
    public CMChannel(String name, List<String> users, List<String> deviceIds) {
        this.name = name;
        this.users = users;
        this.deviceIds = deviceIds;
    }

    /**
     * Get the unique identifier for this channel
     * @return
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The user ids of the users who will receive pushes on this channel
     * @return
     */
    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    /**
     * Add a user by userid to this Channel. Does not make any modifications server side
     * @param userId
     */
    public void addUser(String userId) {
        if(users == null) users = new ArrayList<String>();
        users.add(userId);
    }

    public void addUser(CMUser user) {
        addUser(user.getObjectId());
    }

    /**
     * The device ids that a push on this channel will go to. The device id of an Android device can be acquired by calling
     * {@link com.cloudmine.api.DeviceIdentifier#getUniqueId()}
     * @return
     */
    public List<String> getDeviceIds() {
        return deviceIds;
    }

    public void setDeviceIds(List<String> deviceIds) {
        this.deviceIds = deviceIds;
    }

    public void addDeviceId(String deviceId) {
        if(deviceIds == null) deviceIds = new ArrayList<String>();
        deviceIds.add(deviceId);
    }

    /**
     * Create this channel. Note that this will fail if there is already an existing channel with this channelName
     * @param callback a {@link com.cloudmine.api.rest.callbacks.CMResponseCallback}
     */
    public void create(CMCallback<CMResponse> callback) {
        CMWebService.getService().asyncCreateChannel(this, callback);
    }

    /**
     * Delete this channel. Equivalent to calling the static delete method and passing in channel.getName()
     * @param callback
     */
    public void delete(CMCallback<CMResponse> callback) {
        CMChannel.delete(name, callback);
    }

    @Override
    public String transportableRepresentation() throws ConversionException {
        return JsonUtilities.objectToJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CMChannel cmChannel = (CMChannel) o;

        if (deviceIds != null ? !deviceIds.equals(cmChannel.deviceIds) : cmChannel.deviceIds != null) return false;
        if (name != null ? !name.equals(cmChannel.name) : cmChannel.name != null) return false;
        if (users != null ? !users.equals(cmChannel.users) : cmChannel.users != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (users != null ? users.hashCode() : 0);
        result = 31 * result + (deviceIds != null ? deviceIds.hashCode() : 0);
        return result;
    }
}

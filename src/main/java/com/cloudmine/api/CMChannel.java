package com.cloudmine.api;

import com.cloudmine.api.exceptions.ConversionException;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.Transportable;

import java.util.ArrayList;
import java.util.List;

/**
 * A channel for sending push notifications too. Defines specific useres or devices to receive pushes
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class CMChannel implements Transportable {

    private String name;
    private List<String> users;
    private List<String> deviceIds;

    public CMChannel() {

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public void addUser(String userId) {
        if(users == null) users = new ArrayList<String>();
        users.add(userId);
    }

    public void addUser(CMUser user) {
        addUser(user.getObjectId());
    }

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

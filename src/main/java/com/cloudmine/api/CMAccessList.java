package com.cloudmine.api;

import com.cloudmine.api.persistance.CloudMineObject;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.api.rest.callbacks.Callback;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Allows for other users to access another user's user level data. Instantiate a new access list, specify the permissions,
 * add the users you want to grant said permissions to, save the list, and then add the list's object id to other CMObjects
 * to share them. For more information, see <a href="https://cloudmine.me/docs/data-security#security/user">the CloudMine documentation</a>
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
@CloudMineObject("acl")
public class CMAccessList extends CMObject {

    private final Set<String> userObjectIds = new HashSet<String>();
    private final Set<CMAccessPermission> accessPermissions = EnumSet.noneOf(CMAccessPermission.class);
    /**
     * Create a new CMAccessList that grants no privileges and contains no users
     * @return
     */
    public static CMAccessList CMAccessList(CMUser owner) {
        return new CMAccessList(owner);
    }

    public static CMAccessList CMAccessList(CMUser owner, CMAccessPermission... permissions) {
        return new CMAccessList(owner, permissions);
    }

    protected CMAccessList(CMUser owner) {
        super();
        if(owner == null)
            throw new NullPointerException("Cannot instantiate a new CMAccessList from a null CMUser");
        setSaveWith(owner);
    }

    protected CMAccessList(CMUser owner, CMAccessPermission... permissions) {
        this(owner);
        for(CMAccessPermission permission : permissions) {
            this.accessPermissions.add(permission);
        }
    }

    /**
     * Add this user to this access list, giving them the specified permissions of this list
     * @param user
     */
    public void grantAccessTo(CMUser user) {
        grantAccessTo(user.getObjectId());
    }

    /**
     * Add this user to this access list, giving them the specified permissions of this list
     * @param userObjectId
     */
    public void grantAccessTo(String userObjectId) {
        userObjectIds.add(userObjectId);
    }

    /**
     * Add all the given user object ids to this list, giving them the specified permissions of this list
     * @param userObjectIds the object ids of the users to grant permissions to
     */
    public void grantAccessTo(Collection<String> userObjectIds) {
        this.userObjectIds.addAll(userObjectIds);
    }

    /**
     * Add all the given permissions to this access list, allowing the users associated with this list the enumerated permissions
     * @param permissions the permissions to grant
     */
    public void grantPermissions(CMAccessPermission... permissions) {
        for(CMAccessPermission permission : permissions) {
            this.accessPermissions.add(permission);
        }
    }

    /**
     * Check whether this access list grants access for the given user
     * @param user the user to check
     * @return true if the specified user has access, false otherwise
     */
    public boolean doesAllowAccessTo(CMUser user) {
        if(user == null)
            return false;
        return doesAllowAccessTo(user.getObjectId());
    }

    /**
     * Check whether this access list grants access for the given user
     * @param userId the userId of the user to check
     * @return true if the specified user has access, false otherwise
     */
    public boolean doesAllowAccessTo(String userId) {
        return userObjectIds.contains(userId);
    }

    /**
     * Checks whether this list provides access for the given {@link CMAccessPermission}s.
     * @param permissions the permissions to check
     * @return true if this list provides access for all of the given permissions, false otherwise
     */
    public boolean doesGrantPermissions(CMAccessPermission... permissions) {
        for(CMAccessPermission permission : permissions) {
            if(accessPermissions.contains(permission) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check whether the given user owns this access list
     * @param user
     * @return true if this access list was created attached to the given user
     */
    public boolean isOwnedBy(CMUser user) {
        return getUser().equals(user);
    }

    @Override
    public void save() {
        save(Callback.DO_NOTHING);
    }

    @Override
    public void save(Callback callback) {
        store().saveAccessList(this, callback);
    }

    /**
     * Is equivalent to calling {@link #save()}, since the user associated with the list is set at instantiation
     */
    @Override
    public void saveWithUser(CMUser ignored) {
        save();
    }

    /**
     * Is equivalent to calling {@link #save(Callback)}, since the user associated with the list is set at instantiation
     */
    @Override
    public void saveWithUser(CMUser ignored, Callback callback) {
        save(callback);
    }

    @Override
    public String asJson() {
        return JsonUtilities.objectToJson(this);
    }

}

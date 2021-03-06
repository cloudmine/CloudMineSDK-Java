package com.cloudmine.api;

import com.cloudmine.api.exceptions.CreationException;

import java.util.WeakHashMap;

/**
 * Identifies a store
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class StoreIdentifier {
    private static final WeakHashMap<JavaCMUser, StoreIdentifier> storeMap = new WeakHashMap<JavaCMUser, StoreIdentifier>();
    public static final StoreIdentifier DEFAULT = new StoreIdentifier(ObjectLevel.APPLICATION, null);
    private final ObjectLevel level; //never let this be null
    private final JavaCMUser user;

    public static StoreIdentifier applicationLevel() throws CreationException {
        return DEFAULT;
    }

    /**
     * Get the StoreIdentifier for the given user. If no StoreIdentifier exists, one will be created.
     * @param user the user that objects at the USER level are saved with
     * @throws CreationException if given a null session
     */
    public static StoreIdentifier StoreIdentifier(JavaCMUser user) throws CreationException{
        StoreIdentifier identifier = storeMap.get(user);
        if(identifier == null) {
            identifier = new StoreIdentifier(user);
            storeMap.put(user, identifier);
        }
        return identifier;
    }



    protected StoreIdentifier(ObjectLevel level, JavaCMUser user) throws CreationException {
        if(user == null && ObjectLevel.APPLICATION != level) {
            throw new CreationException("User cannot be null unless we are saving to the application level");
        }
        if(level == null) {
            level = ObjectLevel.UNKNOWN;
        }
        this.level = level;
        this.user = user;
    }



    protected StoreIdentifier(JavaCMUser user) throws CreationException {
        this(ObjectLevel.USER, user);
    }

    /**
     * Create an Application level StoreIdentifier
     * @throws CreationException
     */
    public StoreIdentifier() throws CreationException {
        this(ObjectLevel.APPLICATION, null);
    }

    public boolean isApplicationLevel() {
        return ObjectLevel.APPLICATION == getObjectLevel();
    }

    public boolean isUserLevel() {
        return ObjectLevel.USER == getObjectLevel();
    }

    public boolean isLevel(ObjectLevel level) {
        return getObjectLevel().equals(level);
    }

    public ObjectLevel getObjectLevel() {
        return level;
    }

    public JavaCMUser getUser() {
        return user;
    }

    @Override
    public String toString() {
        return level.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StoreIdentifier that = (StoreIdentifier) o;

        if (level != that.level) return false;
        if (user != null ? !user.equals(that.user) : that.user != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = level != null ? level.hashCode() : 0;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }
}

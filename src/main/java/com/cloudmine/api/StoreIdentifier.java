package com.cloudmine.api;

import com.cloudmine.api.exceptions.CreationException;

import java.util.WeakHashMap;

/**
 * Identifies a store
 * Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class StoreIdentifier {
    private static final WeakHashMap<CMSessionToken, StoreIdentifier> storeMap = new WeakHashMap<CMSessionToken, StoreIdentifier>();
    public static final StoreIdentifier DEFAULT = new StoreIdentifier(ObjectLevel.APPLICATION, null);
    private final ObjectLevel level; //never let this be null
    private final CMSessionToken sessionToken;

    public static StoreIdentifier applicationLevel() throws CreationException {
        return DEFAULT;
    }

    /**
     * Get the StoreIdentifier for the given token. If no StoreIdentifier exists, one will be created.
     * @param session a logged in user's session
     * @return the StoreIdentifier for the given session
     * @throws CreationException if given a null session
     */
    public static StoreIdentifier StoreIdentifier(CMSessionToken session) throws CreationException{
        StoreIdentifier identifier = storeMap.get(session);
        if(identifier == null) {
            identifier = new StoreIdentifier(session);
            storeMap.put(session, identifier);
        }
        return identifier;
    }

    private StoreIdentifier(ObjectLevel level, CMSessionToken session) throws CreationException {
        if(session == null && ObjectLevel.APPLICATION != level) {
            throw new CreationException("User cannot be null unless we are saving to ");
        }
        if(level == null) {
            level = ObjectLevel.UNKNOWN;
        }
        this.level = level;
        this.sessionToken = session;
    }

    StoreIdentifier(CMSessionToken session) throws CreationException {
        this(ObjectLevel.USER, session);
    }

    StoreIdentifier() throws CreationException {
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

    public CMSessionToken getSessionToken() {
        return sessionToken;
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
        if (sessionToken != null ? !sessionToken.equals(that.sessionToken) : that.sessionToken != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = level != null ? level.hashCode() : 0;
        result = 31 * result + (sessionToken != null ? sessionToken.hashCode() : 0);
        return result;
    }
}

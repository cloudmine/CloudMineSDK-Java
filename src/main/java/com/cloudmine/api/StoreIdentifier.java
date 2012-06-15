package com.cloudmine.api;

import com.cloudmine.api.exceptions.CreationException;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 6/14/12, 3:09 PM
 */
public class StoreIdentifier {
    public static final StoreIdentifier DEFAULT = applicationLevel();
    private final ObjectLevel level; //never let this be null
    private final CMUserToken userToken;

    public static StoreIdentifier applicationLevel(){
        return new StoreIdentifier(ObjectLevel.APPLICATION, null);
    }

    private StoreIdentifier(ObjectLevel level, CMUserToken user) {
        if(user == null && ObjectLevel.APPLICATION != level) {
            throw new CreationException("User cannot be null unless we are saving to ");
        }
        if(level == null) {
            level = ObjectLevel.UNKNOWN;
        }
        this.level = level;
        this.userToken = user;
    }

    public StoreIdentifier(CMUserToken user) {
        this(ObjectLevel.USER, user);
    }

    public StoreIdentifier() {
        this(ObjectLevel.APPLICATION, null);
    }

    public boolean isApplicationLevel() {
        return ObjectLevel.APPLICATION == level();
    }

    public boolean isUserLevel() {
        return ObjectLevel.USER == level();
    }

    public ObjectLevel level() {
        return level;
    }

    public CMUserToken userToken() {
        return userToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StoreIdentifier that = (StoreIdentifier) o;

        if (level != that.level) return false;
        if (userToken != null ? !userToken.equals(that.userToken) : that.userToken != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = level != null ? level.hashCode() : 0;
        result = 31 * result + (userToken != null ? userToken.hashCode() : 0);
        return result;
    }
}

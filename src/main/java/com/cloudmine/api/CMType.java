package com.cloudmine.api;

import com.cloudmine.api.rest.Json;
import com.cloudmine.api.rest.JsonUtilities;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 6/4/12, 1:08 PM
 */
public enum CMType implements Json {
    GEO_POINT("geopoint"), FILE("file"), NONE("");

    private final String typeId;

    public static CMType getTypeById(String typeId) {
        if(typeId == null)
            return NONE;
        for(CMType type : CMType.values()) {
            if(typeId.equals(type.typeId())) {
                return type;
            }
        }
        return NONE;
    }

    private CMType(String typeId) {
        this.typeId = typeId;
    }

    public String typeId() {
        return typeId;
    }

    @Override
    public String asJson() {
        return JsonUtilities.createJsonProperty(JsonUtilities.TYPE_KEY, typeId());
    }
}

package com.cloudmine.api;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 6/4/12, 1:08 PM
 */
public enum CloudMineType {
    GEO_POINT("geopoint"), NONE("");

    private final String typeId;

    public static CloudMineType getTypeById(String typeId) {
        if(typeId == null)
            return NONE;
        for(CloudMineType type : CloudMineType.values()) {
            if(typeId.equals(type.typeId())) {
                return type;
            }
        }
        return NONE;
    }

    private CloudMineType(String typeId) {
        this.typeId = typeId;
    }

    public String typeId() {
        return typeId;
    }
}

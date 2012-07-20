package com.cloudmine.api;

import java.util.HashMap;
import java.util.Map;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public enum CMAccessPermission {
    CREATE("c"), READ("r"), UPDATE("u"), DELETE("d");

    private static final Map<String, CMAccessPermission> permissionsMap = new HashMap<String, CMAccessPermission>();
    static {
        for(CMAccessPermission permission : values()) {
            permissionsMap.put(permission.serverRepresentation(), permission);
        }
    }

    /**
     * Can return null if given an invalid server representation
     * @param serverRepresentation
     * @return
     */
    public static CMAccessPermission fromServerRepresentation(String serverRepresentation) {
        return permissionsMap.get(serverRepresentation);
    }

    private final String serverRepresentation;
    private CMAccessPermission(String serverRepresentation) {
        this.serverRepresentation = serverRepresentation;
    }

    public String serverRepresentation() {
        return serverRepresentation;
    }
}

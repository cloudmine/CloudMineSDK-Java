package com.cloudmine.api;

import com.cloudmine.api.rest.CMFileMetaData;
import com.cloudmine.api.rest.Transportable;
import com.cloudmine.api.rest.JsonUtilities;

/**
 * CloudMine specific types, such as geopoint or file. If you instantiate a {@link CMGeoPoint} or a {@link CMFile}, these will
 * be set automatically.
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public enum CMType implements Transportable {
    GEO_POINT("geopoint", CMGeoPoint.class), FILE("file", CMFileMetaData.class), USER("user", JavaCMUser.class), NONE("", Void.class);

    private final String typeId;
    private final Class<? extends CMObject> klass;

    /**
     * Get the enum representation of the typeId. Differs from {@link #valueOf(String)} by handing null and
     * non existent types by returning NONE instead of throwing exceptions
     * @param typeId valid values are "geopoint" or "file", ignoring case. Anything else will return NONE
     * @return GEO_POINT or FILE if given a valid value, NONE otherwise
     */
    public static CMType getTypeById(String typeId) {
        if(typeId == null)
            return NONE;
        for(CMType type : CMType.values()) {
            if(typeId.equalsIgnoreCase(type.getTypeId())) {
                return type;
            }
        }
        return NONE;
    }



    private CMType(String typeId, Class klass) {
        this.typeId = typeId;
        this.klass = klass;
    }

    /**
     * String representation of this CMType
     * @return String representation of this CMType
     */
    public String getTypeId() {
        return typeId;
    }

    @Override
    public String transportableRepresentation() {
        return JsonUtilities.createJsonProperty(JsonUtilities.TYPE_KEY, getTypeId());
    }

    public Class<? extends CMObject> getTypeClass() {
        return klass;
    }
}

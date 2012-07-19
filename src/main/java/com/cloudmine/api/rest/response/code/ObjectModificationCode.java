package com.cloudmine.api.rest.response.code;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public enum ObjectModificationCode {
    SUCCESS(200), INVALID_JSON(400), MISSING_OR_INVALID_CREDENTIALS(401), APPLICATION_ID_NOT_FOUND(404), UNKNOWN(-1);
    private final int statusCode;
    private ObjectModificationCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Get the HTTP status code this enum represents
     * @return
     */
    public int getStatusCode() {
        return statusCode;
    }

    public static ObjectModificationCode codeForStatus(int statusCode) {
        for(ObjectModificationCode code : ObjectModificationCode.values()) { //yes could do lookup in a map here but there aren't many values
            if(statusCode == code.getStatusCode()) {
                return code;
            }
        }
        return UNKNOWN;
    }
}
package com.cloudmine.api.rest.response.code;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public enum FileLoadCode {
    LOAD_SUCCESS(200), MISSING_OR_INVALID_AUTHORIZATION(401), APPLICATION_ID_OR_FILE_NOT_FOUND(404), UNKNOWN(-1);
    private final int statusCode;
    private FileLoadCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Get the HTTP status code this enum represents
     * @return
     */
    public int getStatusCode() {
        return statusCode;
    }

    public static FileLoadCode codeForStatus(int statusCode) {
        for(FileLoadCode code : FileLoadCode.values()) { //yes could do lookup in a map here but there aren't many values
            if(statusCode == code.getStatusCode()) {
                return code;
            }
        }
        return UNKNOWN;
    }
}

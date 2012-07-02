package com.cloudmine.api.rest.response.code;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 6/29/12, 12:49 PM
 */
public enum ObjectLoadCode {
    LOAD_SUCCESS(200), MISSING_OR_INVALID_CREDENTIALS(401), APPLICATION_ID_NOT_FOUND(404), UNKNOWN(-1);
    private final int statusCode;
    private ObjectLoadCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Get the HTTP status code this enum represents
     * @return
     */
    public int getStatusCode() {
        return statusCode;
    }

    public static ObjectLoadCode codeForStatus(int statusCode) {
        for(ObjectLoadCode code : ObjectLoadCode.values()) { //yes could do lookup in a map here but there aren't many values
            if(statusCode == code.getStatusCode()) {
                return code;
            }
        }
        return UNKNOWN;
    }
}
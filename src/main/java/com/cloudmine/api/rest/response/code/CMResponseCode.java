package com.cloudmine.api.rest.response.code;

/**
 * Copyright CloudMine LLC
 */
public enum CMResponseCode { //Logging in, create user, change password, reset password, logout
    CREATED(201), EMAIL_ALREADY_EXISTS(409), INVALID_EMAIL_OR_MISSING_PASSWORD(400), APPLICATION_ID_NOT_FOUND(404), UNKNOWN(-1);
    private final int statusCode;
    private CMResponseCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Get the HTTP status code this enum represents
     * @return
     */
    public int getStatusCode() {
        return statusCode;
    }

    public static CMResponseCode codeForStatus(int statusCode) {
        for(CMResponseCode code : CMResponseCode.values()) { //yes could do lookup in a map here but there aren't many values
            if(statusCode == code.getStatusCode()) {
                return code;
            }
        }
        return UNKNOWN;
    }
}

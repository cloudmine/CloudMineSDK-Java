package com.cloudmine.api.rest.response.code;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public enum LogoutCode {
    LOG_OUT_SUCCES(200), MISSING_OR_INVALID_SESSION(401), APPLICATION_ID_NOT_FOUND(404), UNKNOWN(-1);
    private final int statusCode;
    private LogoutCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Get the HTTP status code this enum represents
     * @return
     */
    public int getStatusCode() {
        return statusCode;
    }

    public static LogoutCode codeForStatus(int statusCode) {
        for(LogoutCode code : LogoutCode.values()) { //yes could do lookup in a map here but there aren't many values
            if(statusCode == code.getStatusCode()) {
                return code;
            }
        }
        return UNKNOWN;
    }
}

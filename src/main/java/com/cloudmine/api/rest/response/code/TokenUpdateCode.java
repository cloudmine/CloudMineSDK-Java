package com.cloudmine.api.rest.response.code;

/**
 * Created with IntelliJ IDEA.
 * User: ethan
 * Date: 1/16/13
 * Time: 10:19 AM
 * To change this template use File | Settings | File Templates.
 */
public enum TokenUpdateCode {
    DEVICE_TOKEN_UPDATE_SUCCESS(200), MISSING_OR_INVALID_AUTHORIZATION(401), APPLICATION_ID_NOT_FOUND(404), UNKNOWN(-1);
    private final int statusCode;
    private TokenUpdateCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Get the HTTP status code this enum represents
     * @return
     */
    public int getStatusCode() {
        return statusCode;
    }

    public static TokenUpdateCode codeForStatus(int statusCode) {
        for(TokenUpdateCode code : TokenUpdateCode.values()) {
            if(statusCode == code.getStatusCode()) {
                return code;
            }
        }
        return UNKNOWN;
    }
}

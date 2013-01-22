package com.cloudmine.api.rest.response.code;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public enum SocialGraphCode {
    QUERY_SUCCESS(200,300), FAILURE(300,505), UNKNOWN(-1, -1);

    private int lowerRange;
    private int upperRange;
    private int statusCode;

    private SocialGraphCode(int lowerRange, int upperRange) {
        this.lowerRange = lowerRange;
        this.upperRange = upperRange;
    }

    private SocialGraphCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Get the HTTP status code this enum represents
     * @return
     */
    public int getStatusCode() {
        return statusCode;
    }

    public static SocialGraphCode codeForStatus(int statusCode) {
        for(SocialGraphCode code : SocialGraphCode.values()) {
            if(statusCode <= code.upperRange && statusCode >= code.lowerRange ) {
                return code;
            }
        }
        return UNKNOWN;
    }
}

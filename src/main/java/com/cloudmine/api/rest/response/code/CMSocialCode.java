package com.cloudmine.api.rest.response.code;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public enum CMSocialCode {
    SUCCESS(200), FAILURE(400), INVALID_TOKEN(401);
    private final int statusCode;

    public static CMSocialCode codeForStatus(int statusCode) {
        for(CMSocialCode code : values()) {
            if(code.statusCode == statusCode) {
                return code;
            }
        }
        if(199 < statusCode && statusCode < 300) {
            return SUCCESS;
        }
        return FAILURE;
    }

    private CMSocialCode(int statusCode) {
        this.statusCode = statusCode;
    }
}

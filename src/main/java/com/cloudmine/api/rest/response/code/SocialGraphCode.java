package com.cloudmine.api.rest.response.code;

/**
 * Created with IntelliJ IDEA.
 * User: ethan
 * Date: 1/18/13
 * Time: 10:42 AM
 * To change this template use File | Settings | File Templates.
 */
public enum SocialGraphCode {
    OK(200);

    private final int statusCode;
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
        return null;
    }
}

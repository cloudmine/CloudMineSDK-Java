package com.cloudmine.api.rest;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class Base64Encoder {
    private static Base64Encoder encoder = new AndroidBase64Encoder();
    public static String encode(String toEncode) {
        return encoder.internalEncode(toEncode);
    }
    protected Base64Encoder() {

    }
    protected String internalEncode(String toEncode) {
        return null;
    }
}

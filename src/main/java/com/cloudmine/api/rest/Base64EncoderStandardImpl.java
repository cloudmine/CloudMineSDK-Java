package com.cloudmine.api.rest;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class Base64EncoderStandardImpl implements Base64Encoder{
    @Override
    public String encode(String toEncode) {
        return null;//DatatypeConverter.printBase64Binary(toEncode.getBytes());
    }
}

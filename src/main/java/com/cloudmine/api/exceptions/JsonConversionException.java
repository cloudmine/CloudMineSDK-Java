package com.cloudmine.api.exceptions;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/21/12, 2:07 PM
 */
public class JsonConversionException extends RuntimeException {
    public JsonConversionException(Throwable t) {
        super(t);
    }

    public JsonConversionException(String message, Throwable t) {
        super(message, t);
    }

    public JsonConversionException(String message) {
        super(message);
    }
}

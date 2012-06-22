package com.cloudmine.api.exceptions;

/**
 * Thrown when unable to convert something to/from json
 * Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class JsonConversionException extends CloudMineException{
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

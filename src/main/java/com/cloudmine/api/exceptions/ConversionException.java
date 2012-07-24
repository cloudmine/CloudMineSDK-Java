package com.cloudmine.api.exceptions;

/**
 * Thrown when there is an issue converting to or from the transportation format.
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class ConversionException extends JsonConversionException {
    public ConversionException(Throwable t) {
        super(t);
    }

    public ConversionException(String message, Throwable t) {
        super(message, t);
    }

    public ConversionException(String message) {
        super(message);
    }
}

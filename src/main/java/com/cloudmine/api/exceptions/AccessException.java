package com.cloudmine.api.exceptions;

/**
 * Thrown if you try to access something that has not been initialized or set properly
 * Copyright CloudMine LLC
 */
public class AccessException extends CreationException {
    public AccessException(String message) {
        super(message);
    }

    public AccessException(String message, Throwable parent) {
        super(message, parent);
    }

    public AccessException(Throwable parent) {
        super(parent);
    }
}

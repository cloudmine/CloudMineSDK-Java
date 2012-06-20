package com.cloudmine.api.exceptions;

/**
 * Thrown when unable to instantiate something
 * Copyright CloudMine LLC
 */
public class CreationException extends CloudMineException{

    public CreationException(String message) {
        super(message);
    }

    public CreationException(String message, Throwable parent) {
        super(message, parent);
    }

    public CreationException(Throwable parent) {
        super(parent);
    }
}

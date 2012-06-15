package com.cloudmine.api.exceptions;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/18/12, 4:17 PM
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

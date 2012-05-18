package com.cloudmine.api.exceptions;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/18/12, 4:17 PM
 */
public class CreationException extends RuntimeException {

    public CreationException(Throwable parent) {
        super(parent);
    }
}

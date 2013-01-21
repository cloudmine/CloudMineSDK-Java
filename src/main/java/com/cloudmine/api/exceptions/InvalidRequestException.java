package com.cloudmine.api.exceptions;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class InvalidRequestException extends  CloudMineException {

    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException(String message, Throwable parent) {
        super(message, parent);
    }

    public InvalidRequestException(Throwable parent) {
        super(parent);
    }
}

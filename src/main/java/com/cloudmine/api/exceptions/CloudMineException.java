package com.cloudmine.api.exceptions;

/**
 * Base exception for CloudMine classes.
 * Copyright CloudMine LLC
 */
public class CloudMineException extends RuntimeException {
    protected CloudMineException(String msg) {
        super(msg);
    }
    protected  CloudMineException(String msg, Throwable cause) {
        super(msg, cause);
    }
    protected CloudMineException(Throwable cause) {
        super(cause);
    }
}

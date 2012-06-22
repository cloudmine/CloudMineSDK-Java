package com.cloudmine.api.exceptions;

/**
 * Base exception for CloudMine classes.
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
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

package com.cloudmine.api.exceptions;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class NotLoggedInException extends CloudMineException {
    public NotLoggedInException(String msg) {
        super(msg);
    }

    public NotLoggedInException(String msg, Throwable cause) {
        super(msg, cause);
    }

    protected NotLoggedInException(Throwable cause) {
        super(cause);
    }
}

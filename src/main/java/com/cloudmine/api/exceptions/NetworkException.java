package com.cloudmine.api.exceptions;

/**
 * Thrown when there was an issue with a network request
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class NetworkException extends CloudMineException {
    public NetworkException(String msg) {
        super(msg);
    }

    public NetworkException(String msg, Throwable cause) {
        super(msg, cause);
    }

    protected NetworkException(Throwable cause) {
        super(cause);
    }
}

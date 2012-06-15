package com.cloudmine.api.exceptions;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 6/14/12, 5:07 PM
 */
public class NotLoggedInException extends CloudMineException {

    public NotLoggedInException() {
        super("Cannot perform this action unless the user has logged in");
    }

    public NotLoggedInException(String msg) {
        super(msg);
    }

    public NotLoggedInException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public NotLoggedInException(Throwable cause) {
        super(cause);
    }
}

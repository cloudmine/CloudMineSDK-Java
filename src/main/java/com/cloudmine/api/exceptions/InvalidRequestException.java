package com.cloudmine.api.exceptions;

/**
 * Created with IntelliJ IDEA.
 * User: ethan
 * Date: 1/18/13
 * Time: 11:25 AM
 * To change this template use File | Settings | File Templates.
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

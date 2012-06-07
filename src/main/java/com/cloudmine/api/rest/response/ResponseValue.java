package com.cloudmine.api.rest.response;

/**
 * Represents the statuses that an insertion of an object can return
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 6/7/12, 3:45 PM
 */
public enum ResponseValue {
    CREATED(), DELETED(), UPDATED(), MISSING();


    /**
     * Works like valueOf, except ignores case and if the response value does not match any ResponseValues,
     * MISSING is returned.
     * @param response The value attached to the inserted object's key in the success response
     * @return CREATED, DELETED, or UPDATED if response.equalsIgnoreCase("created"|"updated"|"deleted") returns true; MISSING otherwise
     */
    public static ResponseValue getValue(String response) {
        if(response == null)
            return MISSING;
        ResponseValue value = ResponseValue.valueOf((response.toUpperCase())); //this will work as long as the values above don't change
        if(value == null)
            return MISSING;
        return value;
    }
}

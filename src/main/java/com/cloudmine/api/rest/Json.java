package com.cloudmine.api.rest;

import com.cloudmine.api.exceptions.JsonConversionException;

/**
 * Anything that can be converted to JSON should implement this interface
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public interface Json {
    /**
     * Convert this object to a json representation
     * @return this object as json
     * @throws JsonConversionException if unable to convert to valid json
     */
    public String asJson() throws JsonConversionException;
}

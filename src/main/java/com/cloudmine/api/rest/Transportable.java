package com.cloudmine.api.rest;

import com.cloudmine.api.exceptions.ConversionException;

/**
 * Anything that can be converted to a transportable string format should implement this interface. Currently
 * this format is JSON, but is not guaranteed to be so.
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public interface Transportable {
    /**
     * Convert this object to a transportable representation
     * @return this object as a string
     * @throws ConversionException if unable to convert to valid string representation
     */
    public String transportableRepresentation() throws ConversionException;
}

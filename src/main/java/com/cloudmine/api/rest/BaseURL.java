package com.cloudmine.api.rest;

/**
 * An entire or partial URL. Should never end with "/"
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public interface BaseURL {
    String SEPARATOR = "/";

    /**
     * A string representation of this URL
     * @return A string representation of this URL
     */
    public String asUrlString();
}

package com.cloudmine.api.rest;

/**
 * An entire or partial URL. Should never end with "/"
 * Copyright CloudMine LLC
 */
public interface BaseURL {
    String SEPARATOR = "/";

    /**
     * A string representation of this URL
     * @return A string representation of this URL
     */
    public String urlString();
}

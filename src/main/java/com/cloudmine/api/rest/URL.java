package com.cloudmine.api.rest;

/**
 * An entire or partial URL. Should never end with "/"
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/16/12, 11:32 AM
 */
public interface URL {
    String SEPARATOR = "/";

    public String url();
}

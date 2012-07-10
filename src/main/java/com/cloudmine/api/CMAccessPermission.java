package com.cloudmine.api;

import com.cloudmine.api.exceptions.JsonConversionException;
import com.cloudmine.api.rest.Json;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public enum CMAccessPermission implements Json{
    CREATE("c"), READ("r"), UPDATE("u"), DELETE("d");

    private final String json;
    private CMAccessPermission(String json) {
        this.json = json;
    }


    @Override
    public String asJson() throws JsonConversionException {
        return json;
    }
}

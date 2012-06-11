package com.cloudmine.api.rest;

/**
 * Copyright CloudMine LLC
 * CMUser: johnmccarthy
 * Date: 5/24/12, 4:32 PM
 */
public class JsonString implements Json {

    private final String json;
    public JsonString(String json) {
        this.json = json;
    }

    public String asJson() {
        return json;
    }

    public String toString() {
        return asJson();
    }
}

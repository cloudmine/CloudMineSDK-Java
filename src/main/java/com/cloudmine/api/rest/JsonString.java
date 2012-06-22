package com.cloudmine.api.rest;

/**
 * An immutable string that is valid JSON.
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class JsonString implements Json {

    private final String json;

    /**
     * Instantiate a new JsonString whose asJson method will return the passed in String.
     * @param json a valid json string. If null, will be replaced with {@link JsonUtilities#EMPTY_JSON}
     */
    public JsonString(String json) {
        if(json == null)
            json = JsonUtilities.EMPTY_JSON;
        this.json = json;
    }

    @Override
    public String asJson() {
        return json;
    }

    @Override
    public String toString() {
        return asJson();
    }
}

package com.cloudmine.api;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 5/16/12, 4:12 PM
 */
public class ApiCredentials {
    private static final String HEADER_KEY = "X-CloudMine-ApiKey";

    private static final ApiCredentials credentials = new ApiCredentials("c1a562ee1e6f4a478803e7b51babe287", "3fc494b36d6d432d9afb051d819bdd72"); //TODO obv this can't stay like this

    private final String applicationIdentifier;
    private final String applicationApiKey;

    private ApiCredentials(String id, String apiKey) {
        applicationIdentifier = id;
        applicationApiKey = apiKey;
    }

    public static ApiCredentials credentials() {
        return credentials;
    }

    public static String applicationIdentifier() {
        return credentials().applicationIdentifier;
    }

    public static Header cloudMineHeader() {
        return new BasicHeader(HEADER_KEY, credentials().applicationApiKey);
    }
}
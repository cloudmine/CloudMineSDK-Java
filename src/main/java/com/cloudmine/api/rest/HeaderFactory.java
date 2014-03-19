package com.cloudmine.api.rest;

import com.cloudmine.api.CMSessionToken;
import org.apache.http.Header;

import java.util.Set;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public interface HeaderFactory {
    public static final String AGENT_HEADER_KEY = "X-CloudMine-Agent";
    public static final String SESSION_TOKEN_HEADER_KEY = "X-CloudMine-SessionToken";
    public static final String DEVICE_HEADER_KEY = "X-CloudMine-UT";
    public static final String REQUEST_ID_KEY = "X-Request-Id";
    public static final String API_HEADER_KEY = "X-CloudMine-ApiKey";
    Set<Header> getCloudMineHeaders(String apiKey);
    Header getUserCloudMineHeader(CMSessionToken token);
    String getCloudMineAgent();
}

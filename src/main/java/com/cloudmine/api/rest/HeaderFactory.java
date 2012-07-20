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

    Set<Header> getCloudMineHeaders();
    Header getUserCloudMineHeader(CMSessionToken token);
    String getCloudMineAgent();
}

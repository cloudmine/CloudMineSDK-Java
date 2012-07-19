package com.cloudmine.api.rest;

import com.cloudmine.api.CMApiCredentials;
import com.cloudmine.api.CMSessionToken;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.util.HashSet;
import java.util.Set;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class HeaderFactoryStandardImpl implements HeaderFactory {
    public static final String CLOUD_MINE_AGENT = "javasdk 1.0";
    @Override
    public Set<Header> getCloudMineHeaders() {
        Set<Header> headerSet = new HashSet<Header>();
        headerSet.add(CMApiCredentials.getCloudMineHeader());
        headerSet.add(new BasicHeader(AGENT_HEADER_KEY, getCloudMineAgent()));
        return headerSet;
    }

    public Header getUserCloudMineHeader(CMSessionToken token) {
        return new BasicHeader(SESSION_TOKEN_HEADER_KEY, token.getSessionToken());
    }

    public String getCloudMineAgent() {
        return CLOUD_MINE_AGENT;
    }
}

package com.cloudmine.api.rest;

import com.cloudmine.api.BaseDeviceIdentifier;
import com.cloudmine.api.CMSessionToken;
import com.cloudmine.api.Strings;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.util.HashSet;
import java.util.Set;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class JavaHeaderFactory implements HeaderFactory {
    public static final String CLOUD_MINE_AGENT = "javasdk 1.0";
    public static final String DEVICE_ID_DELIM = ";";

    private final BaseDeviceIdentifier deviceIdentifier = new BaseDeviceIdentifier();

    @Override
    public Set<Header> getCloudMineHeaders(String apiKey) {
        Set<Header> headerSet = new HashSet<Header>();
        headerSet.add(new BasicHeader(API_HEADER_KEY, apiKey));
        headerSet.add(new BasicHeader(AGENT_HEADER_KEY, getCloudMineAgent()));
        headerSet.add(getDeviceIdentifierHeader());
        return headerSet;
    }

    public Header getUserCloudMineHeader(CMSessionToken token) {
        return new BasicHeader(SESSION_TOKEN_HEADER_KEY, token.getSessionToken());
    }

    protected Header getDeviceIdentifierHeader() {
        String id = getDeviceIdentifier();

        String value = ResponseTimeDataStore.getContentsAsStringAndClearMap();
        String idHeader = Strings.isEmpty(value) ?
                id :
                id + DEVICE_ID_DELIM + value;


        return new BasicHeader(DEVICE_HEADER_KEY, idHeader);
    }


    protected String getDeviceIdentifier() {
        return deviceIdentifier.getUniqueId();
    }

    public String getCloudMineAgent() {
        return CLOUD_MINE_AGENT;
    }
}

package com.cloudmine.api.rest;

import com.cloudmine.api.Strings;
import com.cloudmine.api.rest.callbacks.Callback;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class ResponseTimeDataStore {
    private static final Logger LOG = LoggerFactory.getLogger(ResponseTimeDataStore.class);
    private static final Map<String, Long> responseMap = new ConcurrentHashMap<String, Long>();
    public static final String KEY_VALUE_SEPERATOR = ":";
    public static final String ENTRY_SEPARATOR = ",";

    public static void extractAndStoreResponseTimeInformation(Callback callback, HttpResponse response) {
        long startTime = callback.getStartTime();
        long finishTime = System.currentTimeMillis();
        Long responseTime = Long.valueOf(finishTime - startTime);
        Header[] headers = response.getHeaders(HeaderFactory.REQUEST_ID_KEY);
        if(headers.length == 0) {
            LOG.error("No request id headers returned from server");
            return;
        }
        for(Header header : headers) {
            String requestId = header.getValue();
            responseMap.put(requestId, responseTime);
        }
    }

    public static String getContentsAsStringAndClearMap() {
        StringBuilder responseBuilder = new StringBuilder();
        String separator = "";
        Set<Map.Entry<String, Long>> contents = responseMap.entrySet();
        for(Map.Entry<String, Long> entry : contents) {
            responseBuilder.append(separator).append(entry.getKey()).append(KEY_VALUE_SEPERATOR).append(entry.getValue());
            if(Strings.isEmpty(separator))
                separator = ENTRY_SEPARATOR;
        }
        responseMap.clear(); //we could lose a response if it is added between these two operations, however these headers are not critical so that is alright
        return responseBuilder.toString();
    }

}

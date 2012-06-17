package com.cloudmine.api;

import com.cloudmine.api.rest.BaseURL;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 6/17/12, 3:19 PM
 */
public class CMPagingOptions implements BaseURL {
    public static final CMPagingOptions NONE = new CMPagingOptions("");
    public static final int NO_LIMIT = -1;
    private final int limit;
    private final int skipFirst;
    private final boolean includeCount;
    private final Immutable<String> urlString = new Immutable<String>();

    public CMPagingOptions(String asString) {
        this(0, 0, false); //this is ignored once urlString is set
        urlString.setValue(asString);
    }

    public CMPagingOptions(int limit, int skipFirst, boolean includeCount) {
        this.limit = limit;
        this.skipFirst = skipFirst;
        this.includeCount = includeCount;
    }

    public String urlString() {
        boolean isNotSet = !urlString.isSet();
        if(isNotSet) {

            StringBuilder urlBuilder = new StringBuilder();
            urlString.setValue(urlBuilder.append("limit=").append(limit).append("&skip=").append(skipFirst).append("&count=").append(includeCount).toString());
        }
        return urlString.value();
    }

    public String toString() {
        return urlString();
    }

}

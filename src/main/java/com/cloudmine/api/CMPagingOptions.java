package com.cloudmine.api;

import com.cloudmine.api.rest.BaseURL;

/**
 * Contains options relating to paging for get and search requests. For more information see <a href="https://cloudmine.me/docs/api-reference#ref/object_paging">the CloudMine API documentation on Paging</a>
 * Copyright CloudMine LLC
 */
public class CMPagingOptions implements BaseURL {
    public static final CMPagingOptions NONE = new CMPagingOptions("");
    public static final int NO_LIMIT = -1;
    public static final int DEFAULT_LIMIT = 50;
    public static final String COUNT_KEY = "count";
    private final int limit;
    private final int skipFirst;
    private final boolean includeCount;
    private final Immutable<String> urlString = new Immutable<String>();

    /**
     * Specify the paging options as a URL string. You probably don't want to use this.
     * @param asString the paging options as a string, no leading ?.
     */
    public CMPagingOptions(String asString) {
        this(0, 0, false); //this is ignored once urlString is set
        urlString.setValue(asString);
    }

    /**
     * Instantiate a new CMPagingOptions with the different paging options.
     * @param limit the maximum number of results to return for this request
     * @param skipFirst skip the first skipFirst results
     * @param includeCount true if the response should include the total number of result set for the query, regardless of the values for limit or skip. If true,
     *                     the count is returned as "count": N, and can be queried by CMResponse.getObject(COUNT_KEY)
     */
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

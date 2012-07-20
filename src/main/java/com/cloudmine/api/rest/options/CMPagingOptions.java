package com.cloudmine.api.rest.options;

import com.cloudmine.api.Immutable;
import com.cloudmine.api.rest.BaseURL;

/**
 * Contains options relating to paging for get and search requests. For more information see
 * <a href="https://cloudmine.me/docs/api-reference#ref/object_paging">the CloudMine API documentation on Paging</a>
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
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
    public static CMPagingOptions CMPagingOptions(String asString) {
        return new CMPagingOptions(asString);
    }

    /**
     * Instantiate a new CMPagingOptions with the different paging options.
     * @param limit the maximum number of results to return for this request
     * @param skipFirst skip the first skipFirst results
     * @param includeCount true if the response should include the total number of result set for the query, regardless of the values for limit or skip. If true,
     *                     the count is returned as "count": N, and can be queried by CMResponse.getObject(COUNT_KEY)
     */
    public static CMPagingOptions CMPagingOptions(int limit, int skipFirst, boolean includeCount) {
        return new CMPagingOptions(limit, skipFirst, includeCount);
    }

    CMPagingOptions(int limit, int skipFirst, boolean includeCount) {
        this.limit = limit;
        this.skipFirst = skipFirst;
        this.includeCount = includeCount;
    }

    CMPagingOptions(String asString) {
        this(0, 0, false); //this is ignored once urlString is set
        urlString.setValue(asString);
    }

    public String asUrlString() {
        boolean isNotSet = !urlString.isSet();
        if(isNotSet) {

            StringBuilder urlBuilder = new StringBuilder();
            urlString.setValue(urlBuilder.append("limit=").append(limit).append("&skip=").append(skipFirst).append("&count=").append(includeCount).toString());
        }
        return urlString.value();
    }

    public String toString() {
        return asUrlString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CMPagingOptions that = (CMPagingOptions) o;

        if (includeCount != that.includeCount) return false;
        if (limit != that.limit) return false;
        if (skipFirst != that.skipFirst) return false;
        if (urlString != null ? !urlString.equals(that.urlString) : that.urlString != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = limit;
        result = 31 * result + skipFirst;
        result = 31 * result + (includeCount ? 1 : 0);
        result = 31 * result + (urlString != null ? urlString.hashCode() : 0);
        return result;
    }
}

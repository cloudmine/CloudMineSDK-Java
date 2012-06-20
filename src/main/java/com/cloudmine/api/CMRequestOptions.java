package com.cloudmine.api;

import com.cloudmine.api.rest.BaseURL;

/**
 * A container for the different options that can be passed into a request.
 * Copyright CloudMine LLC
 */
public class CMRequestOptions implements BaseURL{
    public static final CMRequestOptions NONE = new CMRequestOptions(CMPagingOptions.NONE, CMServerFunction.NONE, CMSortOptions.NONE);

    private final CMPagingOptions pagingOptions;
    private final CMServerFunction serverFunction;
    private final CMSortOptions sortOptions;

    /**
     * Instantiate a CMRequestOptions with the specified individual options. If you do not want to specify
     * a specific option, you can pass in the NONE constant from the relevant class.
     * @param pagingOptions the options for paging the results
     * @param serverFunction a server side code snippet to call, and any options associated with that
     * @param sortOptions sorting options for any results
     */
    public static CMRequestOptions CMRequestOptions(CMPagingOptions pagingOptions, CMServerFunction serverFunction, CMSortOptions sortOptions) {
        return new CMRequestOptions(pagingOptions, serverFunction, sortOptions);
    }

    CMRequestOptions(CMPagingOptions pagingOptions, CMServerFunction serverFunction, CMSortOptions sortOptions) {
        this.pagingOptions = pagingOptions;
        this.serverFunction = serverFunction;
        this.sortOptions = sortOptions;
    }

    @Override
    public String asUrlString() {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("?").append(pagingOptions.asUrlString());
        urlBuilder.append("&").append(serverFunction.asUrlString()); //TODO this is broke for NONE
        urlBuilder.append("&").append(sortOptions.asUrlString());
        return urlBuilder.toString();
    }
}

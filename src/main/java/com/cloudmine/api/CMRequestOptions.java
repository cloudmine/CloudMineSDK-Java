package com.cloudmine.api;

import com.cloudmine.api.rest.BaseURL;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 6/17/12, 3:19 PM
 */
public class CMRequestOptions implements BaseURL{
    public static final CMRequestOptions NONE = new CMRequestOptions(CMPagingOptions.NONE, CMServerFunction.NONE, CMSortOptions.NONE);

    private final CMPagingOptions pagingOptions;
    private final CMServerFunction serverFunction;
    private final CMSortOptions sortOptions;

    public CMRequestOptions(CMPagingOptions pagingOptions, CMServerFunction serverFunction, CMSortOptions sortOptions) {
        this.pagingOptions = pagingOptions;
        this.serverFunction = serverFunction;
        this.sortOptions = sortOptions;
    }


    @Override
    public String urlString() {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("?").append(pagingOptions.urlString());
        urlBuilder.append("&").append(serverFunction.urlString());
        urlBuilder.append("&").append(sortOptions.urlString());
        return urlBuilder.toString();
    }
}

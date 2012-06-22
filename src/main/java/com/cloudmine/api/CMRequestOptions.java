package com.cloudmine.api;

import com.cloudmine.api.rest.BaseURL;

/**
 * A container for the different options that can be passed into a request.
 * Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
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

    public static CMRequestOptions CMRequestOptions(CMPagingOptions pagingOptions) {
        return new CMRequestOptions(pagingOptions, CMServerFunction.NONE, CMSortOptions.NONE);
    }

    public static CMRequestOptions CMRequestOptions(CMServerFunction function) {
        return new CMRequestOptions(CMPagingOptions.NONE, function, CMSortOptions.NONE);
    }

    public static CMRequestOptions CMRequestOptions(CMSortOptions sortOptions) {
        return new CMRequestOptions(CMPagingOptions.NONE, CMServerFunction.NONE, sortOptions);
    }

    CMRequestOptions(CMPagingOptions pagingOptions, CMServerFunction serverFunction, CMSortOptions sortOptions) {
        this.pagingOptions = pagingOptions;
        this.serverFunction = serverFunction;
        this.sortOptions = sortOptions;
    }

    @Override
    public String asUrlString() {
        StringBuilder urlBuilder = new StringBuilder();
        boolean hasPaging = !CMPagingOptions.NONE.equals(pagingOptions);
        addIfExists(urlBuilder, pagingOptions);
        addIfExists(urlBuilder, serverFunction);
        addIfExists(urlBuilder, sortOptions);
//        urlBuilder.append("?").append(pagingOptions.asUrlString());
//        urlBuilder.append("&").append(serverFunction.asUrlString()); //TODO this is broke for NONE
//        urlBuilder.append("&").append(sortOptions.asUrlString());
        return urlBuilder.toString();
    }

    private void addIfExists(StringBuilder builder, BaseURL url) {
        if(url.asUrlString().isEmpty()) {
            return;
        }
        boolean isNotEmpty = !builder.toString().isEmpty();
        if(isNotEmpty) {
            builder.append("&");
        }
        builder.append(url.asUrlString());
    }
}

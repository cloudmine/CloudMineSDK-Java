package com.cloudmine.api.rest.options;

import com.cloudmine.api.Strings;
import com.cloudmine.api.rest.BaseURL;

/**
 * A container for the different options that can be passed into a request.
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class CMRequestOptions implements BaseURL{
    public static final CMRequestOptions NONE = new CMRequestOptions(CMPagingOptions.NONE, CMServerFunction.NONE, CMSortOptions.NONE);

    private CMPagingOptions pagingOptions = CMPagingOptions.NONE;
    private CMServerFunction serverFunction = CMServerFunction.NONE;
    private CMSortOptions sortOptions = CMSortOptions.NONE;
    private CMSharedDataOptions sharedDataOptions = CMSharedDataOptions.NO_OPTIONS;
    private CMSearchOptions searchOptions = CMSearchOptions.NONE;


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

    public static CMRequestOptions CMRequestOptions(CMSharedDataOptions sharedDataOptions) {
        return new CMRequestOptions(CMPagingOptions.NONE, CMServerFunction.NONE, CMSortOptions.NONE, sharedDataOptions);
    }

    public CMRequestOptions(CMSharedDataOptions sharedDataOptions) {
        this(CMPagingOptions.NONE, CMServerFunction.NONE, CMSortOptions.NONE, sharedDataOptions);
    }

    public CMRequestOptions(CMSortOptions sortOptions) {
        this(CMPagingOptions.NONE, CMServerFunction.NONE, sortOptions);
    }

    public CMRequestOptions(CMPagingOptions pagingOptions) {
        this(pagingOptions, CMServerFunction.NONE, CMSortOptions.NONE);
    }

    public CMRequestOptions(CMServerFunction serverFunction) {
        this(CMPagingOptions.NONE, serverFunction, CMSortOptions.NONE);
    }

    public CMRequestOptions(CMPagingOptions pagingOptions, CMServerFunction serverFunction, CMSortOptions sortOptions, CMSharedDataOptions sharedDataOptions) {
        this.pagingOptions = pagingOptions;
        this.serverFunction = serverFunction;
        this.sortOptions = sortOptions;
        this.sharedDataOptions = sharedDataOptions;
    }

    public CMRequestOptions(CMSearchOptions searchOptions) {
        this.searchOptions = searchOptions;
    }

    public CMRequestOptions() {

    }

    public CMRequestOptions(CMPagingOptions pagingOptions, CMServerFunction serverFunction, CMSortOptions sortOptions) {
        this(pagingOptions, serverFunction, sortOptions, CMSharedDataOptions.NO_OPTIONS);
    }

    public CMPagingOptions getPagingOptions() {
        return pagingOptions;
    }

    public void setPagingOptions(CMPagingOptions pagingOptions) {
        this.pagingOptions = pagingOptions;
    }

    public CMServerFunction getServerFunction() {
        return serverFunction;
    }

    public CMRequestOptions setServerFunction(CMServerFunction serverFunction) {
        this.serverFunction = serverFunction;
        return this;
    }

    public CMSortOptions getSortOptions() {
        return sortOptions;
    }

    public CMRequestOptions setSortOptions(CMSortOptions sortOptions) {
        this.sortOptions = sortOptions;
        return this;
    }

    public CMSharedDataOptions getSharedDataOptions() {
        return sharedDataOptions;
    }

    public CMRequestOptions setSharedDataOptions(CMSharedDataOptions sharedDataOptions) {
        this.sharedDataOptions = sharedDataOptions;
        return this;
    }

    public CMSearchOptions getSearchOptions() {
        return searchOptions;
    }

    public CMRequestOptions setSearchOptions(CMSearchOptions searchOptions) {
        this.searchOptions = searchOptions;
        return this;
    }

    @Override
    public String asUrlString() {
        StringBuilder urlBuilder = new StringBuilder();
        addIfExists(urlBuilder, searchOptions);
        addIfExists(urlBuilder, pagingOptions);
        addIfExists(urlBuilder, serverFunction);
        addIfExists(urlBuilder, sortOptions);
        addIfExists(urlBuilder, sharedDataOptions);
        return urlBuilder.toString();
    }

    private void addIfExists(StringBuilder builder, BaseURL url) {
        if(url == null || Strings.isEmpty(url.asUrlString())) {
            return;
        }
        boolean isNotEmpty = !Strings.isEmpty(builder.toString());
        if(isNotEmpty) {
            builder.append("&");
        }
        builder.append(url.asUrlString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CMRequestOptions that = (CMRequestOptions) o;

        if (pagingOptions != null ? !pagingOptions.equals(that.pagingOptions) : that.pagingOptions != null)
            return false;
        if (searchOptions != null ? !searchOptions.equals(that.searchOptions) : that.searchOptions != null)
            return false;
        if (serverFunction != null ? !serverFunction.equals(that.serverFunction) : that.serverFunction != null)
            return false;
        if (sharedDataOptions != null ? !sharedDataOptions.equals(that.sharedDataOptions) : that.sharedDataOptions != null)
            return false;
        if (sortOptions != null ? !sortOptions.equals(that.sortOptions) : that.sortOptions != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = pagingOptions != null ? pagingOptions.hashCode() : 0;
        result = 31 * result + (serverFunction != null ? serverFunction.hashCode() : 0);
        result = 31 * result + (sortOptions != null ? sortOptions.hashCode() : 0);
        result = 31 * result + (sharedDataOptions != null ? sharedDataOptions.hashCode() : 0);
        result = 31 * result + (searchOptions != null ? searchOptions.hashCode() : 0);
        return result;
    }
}

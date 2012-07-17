package com.cloudmine.api.rest.options;

import com.cloudmine.api.rest.BaseURL;

/**
 * A container for the different options that can be passed into a request.
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class CMRequestOptions implements BaseURL{
    public static final CMRequestOptions NONE = new CMRequestOptions(CMPagingOptions.NONE, CMServerFunction.NONE, CMSortOptions.NONE);

    private final CMPagingOptions pagingOptions;
    private final CMServerFunction serverFunction;
    private final CMSortOptions sortOptions;
    private final CMSharedDataOptions sharedDataOptions;

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

    public CMRequestOptions(CMPagingOptions pagingOptions, CMServerFunction serverFunction, CMSortOptions sortOptions, CMSharedDataOptions sharedDataOptions) {
        this.pagingOptions = pagingOptions;
        this.serverFunction = serverFunction;
        this.sortOptions = sortOptions;
        this.sharedDataOptions = sharedDataOptions;
    }

    CMRequestOptions(CMPagingOptions pagingOptions, CMServerFunction serverFunction, CMSortOptions sortOptions) {
        this(pagingOptions, serverFunction, sortOptions, CMSharedDataOptions.NO_OPTIONS);
    }

    @Override
    public String asUrlString() {
        StringBuilder urlBuilder = new StringBuilder();
        addIfExists(urlBuilder, pagingOptions);
        addIfExists(urlBuilder, serverFunction);
        addIfExists(urlBuilder, sortOptions);
        addIfExists(urlBuilder, sharedDataOptions);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CMRequestOptions that = (CMRequestOptions) o;

        if (pagingOptions != null ? !pagingOptions.equals(that.pagingOptions) : that.pagingOptions != null)
            return false;
        if (serverFunction != null ? !serverFunction.equals(that.serverFunction) : that.serverFunction != null)
            return false;
        if (sortOptions != null ? !sortOptions.equals(that.sortOptions) : that.sortOptions != null) return false;

        if(sharedDataOptions != null ? !sharedDataOptions.equals(that.sharedDataOptions) : that.sharedDataOptions != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = pagingOptions != null ? pagingOptions.hashCode() : 0;
        result = 31 * result + (serverFunction != null ? serverFunction.hashCode() : 0);
        result = 31 * result + (sortOptions != null ? sortOptions.hashCode() : 0);
        result = 31 * result + (sharedDataOptions != null ? sharedDataOptions.hashCode() : 0);
        return result;
    }
}

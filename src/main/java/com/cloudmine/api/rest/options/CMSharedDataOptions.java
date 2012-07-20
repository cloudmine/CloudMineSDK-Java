package com.cloudmine.api.rest.options;

import com.cloudmine.api.Immutable;
import com.cloudmine.api.rest.BaseURL;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMSharedDataOptions implements BaseURL {

    public static final CMSharedDataOptions SHARED_OPTIONS = new CMSharedDataOptions(true, false);
    public static final CMSharedDataOptions SHARED_ONLY = new CMSharedDataOptions(true, true);
    public static final CMSharedDataOptions NO_OPTIONS = new CMSharedDataOptions("");
    private boolean getShared;
    private boolean getSharedOnly;
    private Immutable<String> urlString = new Immutable<String>();

    public static CMSharedDataOptions getShared() {
        return SHARED_OPTIONS;
    }

    public static CMSharedDataOptions getSharedOnly() {
        return SHARED_ONLY;
    }

    public static CMSharedDataOptions noOptions() {
        return NO_OPTIONS;
    }

    /**
     * You should probably be using one of the static methods or instances to avoid creating a new object
     * @param getShared whether to fetch
     * @param getSharedOnly
     */
    public CMSharedDataOptions(boolean getShared, boolean getSharedOnly) {
        this.getShared = getShared;
        this.getSharedOnly = getSharedOnly;
    }

    protected CMSharedDataOptions(String urlString) {
        this.urlString.setValue(urlString);
    }

    @Override
    public String asUrlString() {
        if(!urlString.isSet()) {
            StringBuilder urlBuilder = new StringBuilder();
            String url;
            if(getShared && !getSharedOnly)
                url = "shared=true";
            else if(getSharedOnly)
                url = "shared_only=true";
            else
                url = "shared=false";
            urlString.setValue(url);
        }
        return urlString.value();
    }
}

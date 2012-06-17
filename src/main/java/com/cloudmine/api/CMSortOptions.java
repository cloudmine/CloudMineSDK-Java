package com.cloudmine.api;

import com.cloudmine.api.rest.BaseURL;

/**
 * Copyright CloudMine LLC
 * User: johnmccarthy
 * Date: 6/17/12, 3:19 PM
 */
public class CMSortOptions implements BaseURL{

    public static final CMSortOptions NONE = new CMSortOptions("");

    enum SortOrder { ASCENDING, DESCENDING };

    private final SortOrder order;
    private final String sortByField;
    private final Immutable<String> urlString = new Immutable<String>();

    public CMSortOptions(SortOrder order, String sortByField) {
        this.order = order;
        this.sortByField = sortByField;
    }

    public CMSortOptions(String asString) {
        urlString.setValue(asString);
        order = null;
        sortByField = null;
    }

    @Override
    public String urlString() {//TODO figure out how this is represented
        return urlString.value();
    }
}

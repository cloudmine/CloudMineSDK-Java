package com.cloudmine.api;

import com.cloudmine.api.rest.BaseURL;

/**
 * Encapsulates options related to sorting, such as the field to sort by and the direction
 * Copyright CloudMine LLC
 */
public class CMSortOptions implements BaseURL{

    public static final CMSortOptions NONE = new CMSortOptions("");

    enum SortDirection { ASCENDING, DESCENDING };

    private final SortDirection direction;
    private final String sortByField;
    private final Immutable<String> urlString = new Immutable<String>();

    /**
     * Instantiate a new CMSortOptions for the specified field, in the specified direction
     * @param sortByField the field on the object to sort by
     * @param direction the direction of the field, ASCENDING or DESCENDING
     */
    public CMSortOptions(String sortByField, SortDirection direction) {
        this.direction = direction;
        this.sortByField = sortByField;
    }

    /**
     * Instantiate a new CMSortOptions using a raw URL string. No leading & or ? necessary.
     * You probably don't want to be calling this
     * @param asString the raw URL string
     */
    public CMSortOptions(String asString) {
        urlString.setValue(asString);
        direction = null;
        sortByField = null;
    }

    @Override
    public String urlString() {//TODO figure out how this is represented
        return urlString.value();
    }
}

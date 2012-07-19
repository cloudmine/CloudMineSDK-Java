package com.cloudmine.api;

import com.cloudmine.api.rest.BaseURL;

/**
 * Encapsulates options related to sorting, such as the field to sort by and the direction
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
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
    public static CMSortOptions CMSortOptions(String sortByField, SortDirection direction) {
        return new CMSortOptions(sortByField, direction);
    }

    /**
     * Instantiate a new CMSortOptions using a raw URL string. No leading & or ? necessary.
     * You probably don't want to be calling this
     * @param asString the raw URL string
     */
    public static CMSortOptions CMSortOptions(String asString) {
        return new CMSortOptions(asString);
    }

    CMSortOptions(String sortByField, SortDirection direction) {
        this.direction = direction;
        this.sortByField = sortByField;
    }

    CMSortOptions(String asString) {
        urlString.setValue(asString);
        direction = null;
        sortByField = null;
    }

    @Override
    public String asUrlString() {//TODO figure out how this is represented
        return urlString.value();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CMSortOptions that = (CMSortOptions) o;

        if (direction != that.direction) return false;
        if (sortByField != null ? !sortByField.equals(that.sortByField) : that.sortByField != null) return false;
        if (urlString != null ? !urlString.equals(that.urlString) : that.urlString != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = direction != null ? direction.hashCode() : 0;
        result = 31 * result + (sortByField != null ? sortByField.hashCode() : 0);
        result = 31 * result + (urlString != null ? urlString.hashCode() : 0);
        return result;
    }
}

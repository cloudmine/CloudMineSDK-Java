package com.cloudmine.api.rest.options;

import com.cloudmine.api.Immutable;
import com.cloudmine.api.rest.BaseURL;

/**
 * Encapsulates options related to sorting, such as the field to sort by and the direction
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 */
public class CMSortOptions implements BaseURL{

    public static class Builder {
        StringBuilder sortString = new StringBuilder();

        public Builder addSortField(String sortByField, SortDirection direction) {
            if(sortString.toString().length() != 0) {
                sortString.append("&");
            }
            addSortString(sortString, sortByField, direction);
            return this;
        }

        public CMSortOptions build() {
            return new CMSortOptions(sortString.toString());
        }
    }

    public static final CMSortOptions NONE = new CMSortOptions("");

    public enum SortDirection { ASCENDING("asc"), DESCENDING("desc");
        private final String representation;
        private SortDirection(String representation) {
            this.representation = representation;
        }

        @Override
        public String toString() {
            return representation;
        }
    };

    private final SortDirection direction;
    private final String sortByField;
    private final Immutable<String> urlString = new Immutable<String>();

    private static void addSortString(StringBuilder builder, String sortByField, SortDirection direction) {
        builder.append("sort=").append(sortByField).append(":").append(direction);
    }

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

    /**
     * Instantiate a new CMSortOptions for the specified field, in the specified direction
     * @param sortByField the field on the object to sort by
     * @param direction the direction of the field, ASCENDING or DESCENDING
     */
    public CMSortOptions(String sortByField, SortDirection direction) {
        this.direction = direction;
        this.sortByField = sortByField;
    }

    CMSortOptions(String asString) {
        urlString.setValue(asString);
        direction = null;
        sortByField = null;
    }

    @Override
    public String asUrlString() {
        if(!urlString.isSet()) {
            StringBuilder urlBuilder = new StringBuilder();
            addSortString(urlBuilder, sortByField, direction);
            urlString.setValue(urlBuilder.toString());
        }
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

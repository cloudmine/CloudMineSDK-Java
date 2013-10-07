package com.cloudmine.api;

import com.cloudmine.api.persistance.ClassNameRegistry;
import com.cloudmine.api.rest.JsonUtilities;

/**
 * Domain specific language for constructing search queries. Note that while most operations are restricted so that
 * only legal queries can be constructed, this is not the case for or'ing and and'ing - you can always OR even if you've
 * already ANDed in the current FilterValue, which is not valid syntax.
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class SearchQuery {

    private StringBuilder queryBuilder = new StringBuilder();
    private String lastPropertyName;

    /**
     * Start constructing a top level filter with the given property name
     * @param name the name of the property
     * @return a {@link PropertyName} that can be used to specify the filter
     */
    public static PropertyName filter(String name){
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.openFilter();
        return searchQuery.PropertyName(name);
    }

    /**
     * Construct a {@link CombinableFilterValue} that will search
     * @param cls
     * @return
     */
    public static CombinableFilterValue filter(Class cls) {
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.openFilter();
        return searchQuery.PropertyName(JsonUtilities.CLASS_KEY).equal(ClassNameRegistry.forClass(cls));
    }

    public static SubObject subObject(String name) {
        return new SearchQuery().SubObject(name);
    }

    public class SubObject {
        private SubObject(String name) {
            queryBuilder.append(name);
        }

        public SubObject subObject(String name) {
            subObjectSeparator();
            return new SubObject(name);
        }

        public PropertyName filter(String name) {
            openFilter();
            return PropertyName(name);
        }
    }

    private void subObjectSeparator() {
        queryBuilder.append(".");
    }

    public class PropertyName {
        //Geopoint filters
        public CMGeoPointFilterValue near(CMGeoPointInterface geoPoint) {
            queryBuilder.append(" near (").append(geoPoint.getLongitude()).append(", ").append(geoPoint.getLatitude()).append(")");
            return new CMGeoPointFilterValue();
        }

        //String filter values
        public CombinableFilterValue notEqual(String value) {
            notEqual();
            quote(value);
            return FilterValue();
        }

        private void notEqual() {
            queryBuilder.append(" != ");
        }

        public CombinableFilterValue equal(String value) {
            equal();
            quote(value);
            return FilterValue();
        }

        public CombinableFilterValue greaterThan(String value) {
            greaterThan();
            quote(value);
            return FilterValue();
        }

        public CombinableFilterValue greaterThanOrEqual(String value) {
            greaterThanOrEqual();
            quote(value);
            return FilterValue();
        }

        public CombinableFilterValue lessThan(String value) {
            lessThan();
            quote(value);
            return FilterValue();
        }

        public CombinableFilterValue lessThanOrEqual(String value) {
            lessThanOrEqual();;
            quote(value);
            return FilterValue();
        }

        //Integer filter values
        public CombinableFilterValue equal(int value) {
            equal();
            queryBuilder.append(value);
            return FilterValue();
        }

        public CombinableFilterValue notEqual(int value) {
            notEqual();
            queryBuilder.append(value);
            return FilterValue();
        }

        public CombinableFilterValue lessThan(int value) {
            lessThan();
            queryBuilder.append(value);
            return FilterValue();
        }

        private void lessThan() {
            queryBuilder.append(" < ");
        }

        public CombinableFilterValue lessThanOrEqual(int value) {
            lessThanOrEqual();
            queryBuilder.append(value);
            return FilterValue();
        }

        private void lessThanOrEqual() {
            queryBuilder.append(" <= ");
        }

        public CombinableFilterValue greaterThan(int value) {
            queryBuilder.append(" > ").append(value);
            return FilterValue();
        }

        public CombinableFilterValue greaterThanOrEqual(int value) {
            greaterThanOrEqual();
            queryBuilder.append(value);
            return FilterValue();
        }

        private void greaterThan() {
            queryBuilder.append(" > ");
        }

        private void greaterThanOrEqual() {
            queryBuilder.append(" >= ");
        }

        private void equal() {
            queryBuilder.append(" = ");
        }

    }

    public interface FilterValue {
        public SubObject subObject(String name);
        public String searchQuery();
    }

    public class CMGeoPointFilterValue implements FilterValue {

        public FilterValue within(Distance distance) {
            return within(distance.getMeasurement(), distance.getUnits());
        }

        public FilterValue within(double distance, DistanceUnits units) {
            queryBuilder.append(", ").append(distance).append(units.name());
            return this;
        }

        public SubObject subObject(String name) {
            closeFilter();
            subObjectSeparator();
            return new SubObject(name);
        }

        public String searchQuery() {
            closeFilter();
            return queryBuilder.toString();
        }

        private void closeFilter() {
            queryBuilder.append("]");
        }
    }

    public interface CombinableFilterValue extends FilterValue{
        public PropertyName and(String name);
        public PropertyName and();
        public PropertyName or(String name);
        public PropertyName or();
    }

    private class CombinableFilterValueImpl implements CombinableFilterValue {

        private CombinableFilterValueImpl() {

        }

        private void closeFilter() {
            queryBuilder.append("]");
        }

        public SubObject subObject(String name) {
            closeFilter();
            subObjectSeparator();
            return new SubObject(name);
        }

        public String searchQuery() {
            closeFilter();
            return queryBuilder.toString();
        }

        public PropertyName and(String name) {
            SearchQuery.this.and();
            return PropertyName(name);
        }

        @Override
        public PropertyName and() {
            SearchQuery.this.and();
            return PropertyName(lastPropertyName);
        }

        @Override
        public PropertyName or(String name) {
            SearchQuery.this.or();
            queryBuilder.append(name);
            return new PropertyName();
        }

        @Override
        public PropertyName or() {
            SearchQuery.this.or();
            return PropertyName(lastPropertyName);
        }
    }

    private PropertyName PropertyName(String name) {
        lastPropertyName = name;
        queryBuilder.append(name);
        return new PropertyName();
    }

    private SubObject SubObject(String name) {
        return new SubObject(name);
    }

    private CombinableFilterValue FilterValue() {
        return new CombinableFilterValueImpl();
    }

    private void or() {
        queryBuilder.append(" or ");
    }

    private void and() {
        queryBuilder.append(", ");
    }

    private void openFilter() {
        queryBuilder.append("[");
    }

    private void quote(String value) {
        queryBuilder.append(Strings.QUOTE).append(value).append(Strings.QUOTE);
    }
}

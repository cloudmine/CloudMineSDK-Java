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
     * Construct a {@link CombinableFilterValue} that will search for objects of the given class
     * @param cls the class to search for
     * @return a {@link CombinableFilterValue} that can either be turned into a search query or further specified
     */
    public static CombinableFilterValue filter(Class cls) {
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.openFilter();
        return searchQuery.PropertyName(JsonUtilities.CLASS_KEY).equal(ClassNameRegistry.forClass(cls));
    }

    /**
     * Construct a {@link SubObject} that lets you create a search on a subobject property
     * @param name the key for the subobject
     * @return
     */
    public static SubObject subObject(String name) {
        return new SearchQuery().SubObject(name);
    }

    /**
     * A subobject is a non string/integer object that is a child of a CMObject. You can either drill down further, to a
     * subobject on the SubObject, or filter on a property on the SubObject
     */
    public class SubObject {
        private SubObject(String name) {
            queryBuilder.append(name);
        }

        /**
         * Drill down further
         * @param name the variable name of the subobject to drill down to
         * @return another SubObject
         */
        public SubObject subObject(String name) {
            subObjectSeparator();
            return new SubObject(name);
        }

        /**
         * Filter on the current subobject
         * @param name the property to filter on
         * @return a PropertyName
         */
        public PropertyName filter(String name) {
            openFilter();
            return PropertyName(name);
        }
    }

    private void subObjectSeparator() {
        queryBuilder.append(".");
    }

    /**
     * Represents the state of a variable name being selected for searching on. Provides various methods for filtering on 
     * that variable name
     */
    public class PropertyName {
        //Geopoint filters

        /**
         * This variable name contains a CMGeoPoint and we want to find all the objects near the specified point 
         * @param geoPoint objects will be returned in order based on how close they are to this point
         * @return a CMGeoPointFilterValue that allows for setting a distance limit
         */
        public CMGeoPointFilterValue near(CMGeoPointInterface geoPoint) {
            return near(geoPoint.getLongitude(), geoPoint.getLatitude());
        }

        public CMGeoPointFilterValue near(double longitude, double latitude) {
            queryBuilder.append(" near (").append(longitude).append(", ").append(latitude).append(")");
            return new CMGeoPointFilterValue();

        }

        //String filter values

        /**
         * Find objects with values not equal to the given value
         * @param value the value to compare to this variable name
         * @return a {@link CombinableFilterValue} that lets you add additional filters
         */
        public CombinableFilterValue notEqual(String value) {
            notEqual();
            quote(value);
            return FilterValue();
        }

        private void notEqual() {
            queryBuilder.append(" != ");
        }

        /**
         * Find objects with values equal to the given value
         * @param value the value to compare to this variable name
         * @return a {@link CombinableFilterValue} that lets you add additional filters
         */
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
        /**
         * Find objects with values equal to the given value
         * @param value the value to compare to this variable name
         * @return a {@link CombinableFilterValue} that lets you add additional filters
         */
        public CombinableFilterValue equal(int value) {
            equal();
            queryBuilder.append(value);
            return FilterValue();
        }

        /**
         * Find objects with values not equal to the given value
         * @param value the value to compare to this variable name
         * @return a {@link CombinableFilterValue} that lets you add additional filters
         */
        public CombinableFilterValue notEqual(int value) {
            notEqual();
            queryBuilder.append(value);
            return FilterValue();
        }

        /**
         * Find objects with values less than the given value
         * @param value the value to compare to this variable name
         * @return a {@link CombinableFilterValue} that lets you add additional filters
         */
        public CombinableFilterValue lessThan(int value) {
            lessThan();
            queryBuilder.append(value);
            return FilterValue();
        }

        private void lessThan() {
            queryBuilder.append(" < ");
        }

        /**
         * Find objects with values less than or equal to to the given value
         * @param value the value to compare to this variable name
         * @return a {@link CombinableFilterValue} that lets you add additional filters
         */
        public CombinableFilterValue lessThanOrEqual(int value) {
            lessThanOrEqual();
            queryBuilder.append(value);
            return FilterValue();
        }

        private void lessThanOrEqual() {
            queryBuilder.append(" <= ");
        }

        /**
         * Find objects with values greater than the given value
         * @param value the value to compare to this variable name
         * @return a {@link CombinableFilterValue} that lets you add additional filters
         */
        public CombinableFilterValue greaterThan(int value) {
            queryBuilder.append(" > ").append(value);
            return FilterValue();
        }


        /**
         * Find objects with values greater than or equal to the given value
         * @param value the value to compare to this variable name
         * @return a {@link CombinableFilterValue} that lets you add additional filters
         */
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

    /**
     * A completely specified filter, than can be further specified on a subObject or turned into a searchQuery string
     */
    public interface FilterValue {

        /**
         * Drill down further
         * @param name the variable name of the subobject to drill down to
         * @return another SubObject
         */
        public SubObject subObject(String name);

        /**
         * Return a valid search query for sending to CloudMine
         * @return
         */
        public String searchQuery();
    }

    /**
     * A {@link FilterValue} specific to geo queries
     */
    public class CMGeoPointFilterValue implements FilterValue {

        /**
         * Only return objects within the specified distance
         * @param distance
         * @return
         */
        public FilterValue within(Distance distance) {
            return within(distance.getMeasurement(), distance.getUnits());
        }

        /**
         * Only return objects within the specified distance
         * @param distance
         * @return
         */
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

    /**
     * A filter that can be combined with other filters. Note that only one of AND or OR may be used in a combined filter,
     * although the other methods are still available
     */
    public interface CombinableFilterValue extends FilterValue{
        /**
         * Add another filter on a Property with the specified name
         * @param name the variable name to filter on
         * @return
         */
        public PropertyName and(String name);

        /**
         * Add another filter on the same property that was just filtered on
         * @return
         */
        public PropertyName and();

        /**
         * Add a filter to be or'ed with the proceeding filters with the given name
         * @param name the variable name to filter on
         * @return
         */
        public PropertyName or(String name);

        /**
         * Add a filter to be or'ed with the proceeding filters with the same name as the last filter
         * @return
         */
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

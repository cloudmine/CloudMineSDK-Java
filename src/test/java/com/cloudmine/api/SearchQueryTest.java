package com.cloudmine.api;

import com.cloudmine.api.persistance.ClassNameRegistry;
import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.test.ExtendedCMObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class SearchQueryTest {

    @Test
    public void testBasicSearch() {
        String query = SearchQuery.filter(JsonUtilities.CLASS_KEY).notEqual("govna").searchQuery();
        assertEquals(query(JsonUtilities.CLASS_KEY, "govna", "!="), query);

        query = SearchQuery.filter(JsonUtilities.CLASS_KEY).notEqual("govna").subObject("otherExtendedObjects").subObject("josh").filter("name").equal("candy").searchQuery();
        assertEquals(query(JsonUtilities.CLASS_KEY, "govna", "!=") + ".otherExtendedObjects.josh[name = \"candy\"]", query);
    }

    @Test
    public void testNumberFiltering() {
        String query = SearchQuery.filter("number").equal(5).or("number").greaterThan(100).or().notEqual(10).or().lessThan(33).or().lessThanOrEqual(33).or().greaterThanOrEqual(50).searchQuery();
        assertEquals("[number = 5 or number > 100 or number != 10 or number < 33 or number <= 33 or number >= 50]", query);
    }

    @Test
    public void testGeoFiltering() {
        String query = SearchQuery.filter("location").near(new CMGeoPoint(33, 55)).searchQuery();
        assertEquals("[location near (33.0, 55.0)]", query);

        query = SearchQuery.subObject("house").filter("location").near(new CMGeoPoint(2.5, 3.1)).within(300, DistanceUnits.km).subObject("sub").filter("name").equal("bobbin").or().equal("weez").searchQuery();
        assertEquals("house[location near (2.5, 3.1), 300.0km].sub[name = \"bobbin\" or name = \"weez\"]", query);
    }

    @Test
    public void testAndingOringSearch() {
        String query = SearchQuery.subObject("otherExtendedObjects").subObject("josh").filter("name").notEqual("candy").and("number").lessThan(5).searchQuery();
        assertEquals("otherExtendedObjects.josh[name != \"candy\", number < 5]", query);

        query = SearchQuery.subObject("otherExtendedObjects").subObject("josh").filter("name").notEqual("candy").or("number").lessThan(5).searchQuery();
        assertEquals("otherExtendedObjects.josh[name != \"candy\" or number < 5]", query);

        ClassNameRegistry.register(ExtendedCMObject.CLASS_NAME, ExtendedCMObject.class);

        query = SearchQuery.filter(ExtendedCMObject.class).and("name").equal("default").searchQuery();
        assertEquals("[__class__ = \"govna\", name = \"default\"]", query);
    }

    private String query(String name, String value) {
        return query(name, value, "=");
    }

    private String query(String name, String value, String operation) {
        return "[" + name + " " + operation + " \"" + value + "\"]";
    }
}

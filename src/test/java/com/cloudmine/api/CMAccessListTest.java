package com.cloudmine.api;

import com.cloudmine.api.rest.JsonUtilities;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static junit.framework.Assert.*;

/**
 * <br>
 * Copyright CloudMine LLC. All rights reserved<br>
 * See LICENSE file included with SDK for details.
 */
public class CMAccessListTest {

    @Test
    public void testAddUser() {
        JavaCMUser owner = new JavaCMUser("name@w.com", "t");
        owner.setObjectId(CMObject.generateUniqueObjectId());
        JavaAccessListController list = new JavaAccessListController(owner);
        JavaCMUser userToAddDirectly = new JavaCMUser("user@u.com", "pw");
        userToAddDirectly.setObjectId(CMObject.generateUniqueObjectId());
        JavaCMUser userToAddById = new JavaCMUser("another@u.com", "pw");
        userToAddById.setObjectId(CMObject.generateUniqueObjectId());

        list.grantAccessTo(userToAddDirectly);
        list.grantAccessTo(userToAddById.getObjectId());

        assertTrue(list.doesAllowAccessTo(userToAddDirectly));
        assertTrue(list.doesAllowAccessTo(userToAddById));

        assertTrue(list.doesAllowAccessTo(userToAddDirectly.getObjectId()));
        assertFalse(list.doesAllowAccessTo(new JavaCMUser("nope@not.com", "f")));
    }

    @Test
    public void testIsOwner() {
        JavaCMUser owner = new JavaCMUser("wat@wat.com", "ff");
        JavaAccessListController list = new JavaAccessListController(owner);
        assertTrue(list.isOwnedBy(owner));
        JavaCMUser notOwner = new JavaCMUser("wat@wat.com", "f");
        assertFalse(list.isOwnedBy(notOwner));
    }

    @Test
    public void testPermissions() {
        JavaCMUser owner = new JavaCMUser("whatever@gmail.com", "t");
        JavaAccessListController list = new JavaAccessListController(owner, CMAccessPermission.UPDATE, CMAccessPermission.READ);

        assertTrue(list.doesGrantPermissions(CMAccessPermission.UPDATE, CMAccessPermission.READ));
        assertFalse(list.doesGrantPermissions(CMAccessPermission.DELETE));
        assertFalse(list.doesGrantPermissions(CMAccessPermission.READ, CMAccessPermission.CREATE));
    }

    @Test
    public void testAsJson() {

        JavaCMUser owner = new JavaCMUser("bobdole@bobdole.com", "imbobdole");
        JavaAccessListController list = new JavaAccessListController(owner);
        list.grantPermissions(CMAccessPermission.CREATE);

        String json = list.transportableRepresentation();

        JavaAccessListController convertedList = JsonUtilities.jsonToClass(json, JavaAccessListController.class);
        assertEquals(convertedList,  list);

        Map<String, Object> asMap = JsonUtilities.jsonToMap(json);
        List permissions = (List)asMap.get("permissions");
        assertEquals(1, permissions.size());
        assertEquals("c", permissions.get(0));
    }
}

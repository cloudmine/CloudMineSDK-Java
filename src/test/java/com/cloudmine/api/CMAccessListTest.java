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
        CMUser owner = new CMUser("name@w.com", "t");
        owner.setObjectId(CMObject.generateUniqueObjectId());
        CMAccessList list = new CMAccessList(owner);
        CMUser userToAddDirectly = new CMUser("user@u.com", "pw");
        userToAddDirectly.setObjectId(CMObject.generateUniqueObjectId());
        CMUser userToAddById = new CMUser("another@u.com", "pw");
        userToAddById.setObjectId(CMObject.generateUniqueObjectId());

        list.grantAccessTo(userToAddDirectly);
        list.grantAccessTo(userToAddById.getObjectId());

        assertTrue(list.doesAllowAccessTo(userToAddDirectly));
        assertTrue(list.doesAllowAccessTo(userToAddById));

        assertTrue(list.doesAllowAccessTo(userToAddDirectly.getObjectId()));
        assertFalse(list.doesAllowAccessTo(new CMUser("nope@not.com", "f")));
    }

    @Test
    public void testIsOwner() {
        CMUser owner = new CMUser("wat@wat.com", "ff");
        CMAccessList list = new CMAccessList(owner);
        assertTrue(list.isOwnedBy(owner));
        CMUser notOwner = new CMUser("wat@wat.com", "f");
        assertFalse(list.isOwnedBy(notOwner));
    }

    @Test
    public void testPermissions() {
        CMUser owner = new CMUser("whatever@gmail.com", "t");
        CMAccessList list = new CMAccessList(owner, CMAccessPermission.UPDATE, CMAccessPermission.READ);

        assertTrue(list.doesGrantPermissions(CMAccessPermission.UPDATE, CMAccessPermission.READ));
        assertFalse(list.doesGrantPermissions(CMAccessPermission.DELETE));
        assertFalse(list.doesGrantPermissions(CMAccessPermission.READ, CMAccessPermission.CREATE));
    }

    @Test
    public void testAsJson() {

        CMUser owner = new CMUser("bobdole@bobdole.com", "imbobdole");
        CMAccessList list = new CMAccessList(owner);
        list.grantPermissions(CMAccessPermission.CREATE);

        String json = list.transportableRepresentation();

        CMAccessList convertedList = JsonUtilities.jsonToClass(json, CMAccessList.class);
        assertEquals(convertedList,  list);

        Map<String, Object> asMap = JsonUtilities.jsonToMap(json);
        List permissions = (List)asMap.get("permissions");
        assertEquals(1, permissions.size());
        assertEquals("c", permissions.get(0));
    }
}

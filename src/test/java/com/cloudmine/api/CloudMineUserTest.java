package com.cloudmine.api;

import com.cloudmine.api.rest.JsonUtilities;
import com.cloudmine.test.ExtendedCMUser;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * <br>Copyright CloudMine LLC. All rights reserved<br> See LICENSE file included with SDK for details.
 * CMUser: johnmccarthy
 * Date: 5/21/12, 12:12 PM
 */
public class CloudMineUserTest {


    @Test
    public void testProfileJson() {
        ExtendedCMUser user = new ExtendedCMUser("f@f.com", "1");
        user.setAge(5);
        user.setAddress("whatever");
        user.setPaid(false);

        String expectedJson = "\n" +
                "{\n" +
                "\"age\":5,\n" +
                "\"address\":\"whatever\",\n" +
                "\"paid\":false,\n" +
                 JsonUtilities.createJsonProperty(JsonUtilities.CLASS_KEY, ExtendedCMUser.class.getName()) +
                "}";
        assertTrue(JsonUtilities.isJsonEquivalent(expectedJson, user.profileTransportRepresentation()));
    }
}

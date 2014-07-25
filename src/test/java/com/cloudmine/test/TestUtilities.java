package com.cloudmine.test;

import com.cloudmine.api.rest.JsonUtilities;

import static junit.framework.Assert.assertTrue;

/**
 * <br>Copyright CloudMine LLC. All rights reserved
 * <br> See LICENSE file included with SDK for details.
 */
public class TestUtilities {
    public static void compareJson(String expected, String actual) {
        assertTrue(expected  + " \nshould equal\n" + actual, JsonUtilities.isJsonEquivalent(expected, actual));
    }
}
